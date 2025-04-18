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
import tv.dustypig.dustypig.global_managers.ArtworkCache
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.download_manager.MyDownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.UIJob
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
    private val downloadManager: MyDownloadManager,
    castManager: CastManager,
    routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), RouteNavigator by routeNavigator {


    private val _mediaId: Int = savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_MEDIA_ID)
    private var _parentId: Int = savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_PARENT_ID)
    private val _fromSource: Int = savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_SOURCE)
    private val _playlistUpNextIndex: Int = savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_PLAYLIST_UPNEXT_INDEX_ID)
    private val _canPlay: Boolean = savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_CAN_PLAY)

    private val _cachedArtworkUrl = ArtworkCache.getMediaBackdrop(_mediaId) ?: ""

    private val _uiState = MutableStateFlow(
        EpisodeDetailsUIState(
            artworkUrl = _cachedArtworkUrl,
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

    private var _detailedEpisode: DetailedEpisode? = null

    init {

        viewModelScope.launch {
            PlayerStateManager.playbackEnded.collectLatest {
                updateData()
            }
        }

        viewModelScope.launch {
            downloadManager.currentDownloads.collectLatest { listOfJobs ->
                updateDownloadStatus(listOfJobs)
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        ArtworkCache.deleteMedia(_mediaId)
    }


    private suspend fun updateData() {
        try {
            _detailedEpisode = episodesRepository.details(_mediaId)

            //If this came from a notification, the parent hasn't been set yet
            if(_parentId < 1)
                _parentId = _detailedEpisode!!.seriesId

            _uiState.update {
                it.copy(
                    mediaId = _mediaId,
                    loading = false,
                    canPlay = _canPlay,
                    episodeTitle = _detailedEpisode!!.fullDisplayTitle(),
                    overview = _detailedEpisode!!.description ?: "No description",
                    seriesTitle = _detailedEpisode!!.seriesTitle!!,
                    showGoToSeries = _fromSource != EpisodeDetailsNav.SOURCE_SERIES_DETAILS,
                    length = _detailedEpisode!!.length.toTimeString()
                )
            }

            if(_detailedEpisode!!.artworkUrl != _cachedArtworkUrl) {
                _uiState.update {
                    it.copy(
                        artworkUrl = _detailedEpisode!!.artworkUrl
                    )
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
        var status = DownloadStatus.None
        var percent = 0f
        var downloadingForPlaylistOrSeries = false
        for(job in listOfJobs) {
            for(dl in job.downloads) {
                if(dl.mediaId == _mediaId) {
                    percent = dl.percent
                    status = dl.status
                    if(job.mediaType == MediaTypes.Series || job.mediaType == MediaTypes.Playlist)
                        downloadingForPlaylistOrSeries = true
                }
            }
        }
        _uiState.update {
            it.copy(
                downloadStatus = status,
                downloadPercent = percent,
                downloadingForPlaylistOrSeries = downloadingForPlaylistOrSeries
            )
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
            downloadManager.addEpisode(_detailedEpisode!!)
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
                mediaId = _parentId,
                sourceType =
                    if (_fromSource == EpisodeDetailsNav.SOURCE_PLAYLIST_DETAILS)
                        PlayerNav.MEDIA_TYPE_PLAYLIST
                    else
                        PlayerNav.MEDIA_TYPE_SERIES,
                upNextId =
                    if (_fromSource == EpisodeDetailsNav.SOURCE_PLAYLIST_DETAILS)
                        _playlistUpNextIndex
                    else
                        _mediaId
            )
        )
    }

    private fun addToPlaylist() = navigateToRoute(
        AddToPlaylistNav.getRouteForId(
            id = _mediaId,
            isSeries = false
        )
    )

    private fun goToSeries() = navigateToRoute(SeriesDetailsNav.getRoute(_parentId))
}





















