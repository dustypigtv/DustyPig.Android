package tv.dustypig.dustypig.ui.main_app.screens.episode_details

import androidx.annotation.OptIn
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.repositories.EpisodesRepository
import tv.dustypig.dustypig.api.toTimeString
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.download_manager.UIJob
import tv.dustypig.dustypig.global_managers.media_cache_manager.MediaCacheManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class EpisodeDetailsViewModel
@Inject constructor(
    private val episodesRepository: EpisodesRepository,
    private val downloadManager: DownloadManager,
    castManager: CastManager,
    routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        EpisodeDetailsUIState(
            castManager = castManager,
            onHideError = ::hideError,
            onPopBackStack = ::popBackStack,
            onRemoveDownload = ::removeDownload,
            onPlay = ::play,
            onAddToPlaylist = ::addToPlaylist,
            onAddDownload = ::addDownload,
            onGoToSeries = ::goToSeries
        )
    )
    val uiState: StateFlow<EpisodeDetailsUIState> = _uiState.asStateFlow()

    private val _detailedCacheId: String =
        savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_DETAILED_CACHE_ID)
    private val _fromSeriesDetails: Boolean =
        savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_FROM_SERIES_DETAILS)
    private val _playlistUpNextIndex: Int =
        savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_PLAYLIST_UPNEXT_INDEX_ID)
    private val _basicCacheId: String =
        savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_BASIC_CACHE_ID)
    private val _mediaId: Int = savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_MEDIA_ID)
    private val _canPlay: Boolean = savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_CAN_PLAY)

    private var _detailedEpisode = DetailedEpisode()
    private var _firstLoad = true

    init {

        val cachedInfo = MediaCacheManager.getBasicInfo(_basicCacheId)
        _uiState.update {
            it.copy(
                artworkUrl = cachedInfo.backdropUrl ?: "",
                episodeTitle = cachedInfo.title
            )
        }

        viewModelScope.launch {
            PlayerStateManager.playbackEnded.collectLatest {
                updateData()
            }
        }

        viewModelScope.launch {
            downloadManager.downloads.collectLatest { listOfJobs ->
                updateDownloadStatus(listOfJobs)
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        MediaCacheManager.BasicInfo.removeAll { it.cacheId == _basicCacheId }
    }

    private suspend fun updateData() {
        try {
            _detailedEpisode = if (_firstLoad && _fromSeriesDetails) {
                val detailedSeries = MediaCacheManager.Series[_detailedCacheId]
                detailedSeries!!.episodes!!.first { it.id == _mediaId }
            } else {
                episodesRepository.details(_mediaId)
            }
            _firstLoad = false

            _uiState.update {
                it.copy(
                    mediaId = _mediaId,
                    loading = false,
                    canPlay = _canPlay,
                    episodeTitle = _detailedEpisode.fullDisplayTitle(),
                    overview = _detailedEpisode.description ?: "No description",
                    seriesTitle = _detailedEpisode.seriesTitle!!,
                    showGoToSeries = !_fromSeriesDetails,
                    length = _detailedEpisode.length.toTimeString()
                )
            }

            // Prevent flicker by only updating if needed
            if (_detailedEpisode.artworkUrl != _uiState.value.artworkUrl) {
                _uiState.update {
                    it.copy(artworkUrl = _detailedEpisode.artworkUrl)
                }
            }
        } catch (ex: Exception) {
            ex.logToCrashlytics()
            _uiState.update {
                it.copy(
                    showErrorDialog = true,
                    criticalError = true,
                    errorMessage = ex.message
                )
            }
        }
    }

    private fun updateDownloadStatus(listOfJobs: List<UIJob>) {
        val job = listOfJobs.firstOrNull {
            it.mediaId == _mediaId && it.mediaType == MediaTypes.Episode
        }
        if (job == null) {
            _uiState.update {
                it.copy(downloadStatus = DownloadStatus.None)
            }
        } else {
            _uiState.update {
                it.copy(downloadStatus = job.status)
            }
        }
    }

    private fun hideError() {
        if (_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }

    private fun addDownload() {
        viewModelScope.launch {
            downloadManager.addEpisode(_detailedEpisode)
        }
    }

    private fun removeDownload() {
        viewModelScope.launch {
            downloadManager.delete(_mediaId, MediaTypes.Episode)
        }
    }

    private fun play() {
        navigateToRoute(
            PlayerNav.getRoute(
                mediaId =
                if (_fromSeriesDetails)
                    _detailedEpisode.seriesId
                else
                    MediaCacheManager.Playlists[_detailedCacheId]!!.id,
                sourceType =
                if (_fromSeriesDetails)
                    PlayerNav.MEDIA_TYPE_SERIES
                else
                    PlayerNav.MEDIA_TYPE_PLAYLIST,
                upNextId =
                if (_fromSeriesDetails)
                    _mediaId
                else
                    _playlistUpNextIndex
            )
        )
    }

    private fun addToPlaylist() {
        navigateToRoute(AddToPlaylistNav.getRouteForId(id = _mediaId, isSeries = false))
    }

    private fun goToSeries() {
        val cacheId = MediaCacheManager.add(
            title = _detailedEpisode.seriesTitle ?: "",
            posterUrl = "",
            backdropUrl = null
        )
        navigateToRoute(
            SeriesDetailsNav.getRoute(
                mediaId = _detailedEpisode.seriesId,
                basicCacheId = cacheId
            )
        )
    }
}