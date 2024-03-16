package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import android.annotation.SuppressLint
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
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.GenrePair
import tv.dustypig.dustypig.api.models.Genres
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.api.repositories.MoviesRepository
import tv.dustypig.dustypig.api.toTimeString
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.media_cache_manager.MediaCacheManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title.ManageParentalControlsForTitleNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import tv.dustypig.dustypig.ui.main_app.screens.show_more.ShowMoreNav
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@SuppressLint("SimpleDateFormat")
@HiltViewModel
class MovieDetailsViewModel @OptIn(UnstableApi::class) @Inject constructor(
    routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle,
    castManager: CastManager,
    private val mediaRepository: MediaRepository,
    private val moviesRepository: MoviesRepository,
    private val downloadManager: DownloadManager
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        MovieDetailsUIState(
            castManager = castManager,
            onAddDownload = ::addDownload,
            onAddToPlaylist = ::addToPlaylist,
            onHideError = ::hideError,
            onManagePermissions = ::managePermissions,
            onMarkWatched = ::markWatched,
            onPlay = ::play,
            onPopBackStack = ::popBackStack,
            onRemoveDownload = ::removeDownload,
            onRequestAccess = ::requestAccess,
            onToggleWatchlist = ::toggleWatchList
        )
    )
    val uiState: StateFlow<MovieDetailsUIState> = _uiState.asStateFlow()

    private val mediaId: Int = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_MEDIA_ID)
    private val _basicCacheId: String = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_BASIC_CACHE_ID)
    private val _detailedPlaylistCacheId: String = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_DETAILED_PLAYLIST_CACHE_ID)
    private val _fromPlaylistDetails: Boolean = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_FROM_PLAYLIST_ID)
    private val _playlistUpNextIndex: Int = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_PLAYLIST_UPNEXT_INDEX_ID)

    private var _detailedMovie = DetailedMovie()
    private val _detailCacheId = UUID.randomUUID().toString()


    init {
        val cachedInfo = MediaCacheManager.getBasicInfo(_basicCacheId)
        _uiState.update {
            it.copy(
                posterUrl = cachedInfo.posterUrl,
                backdropUrl = cachedInfo.backdropUrl ?: ""
            )
        }

        viewModelScope.launch {
            PlayerStateManager.playbackEnded.collectLatest {
                updateData()
            }
        }

        viewModelScope.launch {
            downloadManager.downloads.collectLatest { jobLst ->
                val job = jobLst.firstOrNull {
                    it.mediaId == mediaId && it.mediaType == MediaTypes.Movie
                }
                _uiState.update {
                    it.copy (
                        downloadStatus = job?.status ?: DownloadStatus.None
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        MediaCacheManager.BasicInfo.removeAll { it.cacheId == _basicCacheId }
        MediaCacheManager.Movies.remove(_detailCacheId)
    }

    private suspend fun updateData() {
        val cachedInfo = MediaCacheManager.getBasicInfo(_basicCacheId)
        _uiState.update {
            it.copy(
                title = cachedInfo.title
            )
        }

        try {
            _detailedMovie = moviesRepository.details(mediaId)
            MediaCacheManager.Movies[_detailCacheId] = _detailedMovie

            _uiState.update {
                it.copy(
                    loading = false,
                    creditsData = CreditsData(
                        genres = Genres(_detailedMovie.genres).toList(),
                        genreNav = ::genreNav,
                        castAndCrew = _detailedMovie.credits ?: listOf(),
                        personNav = ::personNav,
                        owner = _detailedMovie.owner ?: ""
                    )
                )
            }

            // Prevent flicker by only updating if needed
            if (_detailedMovie.artworkUrl != _uiState.value.posterUrl || _detailedMovie.backdropUrl != _uiState.value.backdropUrl) {
                _uiState.update {
                    it.copy(
                        posterUrl = _detailedMovie.artworkUrl,
                        backdropUrl = _detailedMovie.backdropUrl ?: ""
                    )
                }
            }

            val cal = Calendar.getInstance()
            cal.time = _detailedMovie.date

            _uiState.update {
                it.copy(
                    inWatchList = _detailedMovie.inWatchlist,
                    title = _detailedMovie.title,
                    year = cal.get(Calendar.YEAR).toString(),
                    canManage = _detailedMovie.canManage,
                    canPlay = _detailedMovie.canPlay,
                    rated = _detailedMovie.rated.toString(),
                    length = _detailedMovie.length.toTimeString(),
                    partiallyPlayed = (_detailedMovie.played ?: 0.0) > 0.0,
                    overview = _detailedMovie.description ?: "",
                    titleRequestPermissions = _detailedMovie.titleRequestPermission,
                    accessRequestStatus = _detailedMovie.accessRequestedStatus,
                    accessRequestBusy = false,
                )
            }
        } catch (ex: Exception) {
            setError(ex = ex, criticalError = true)
        }
    }

    private fun setError(ex: Exception, criticalError: Boolean) {
        ex.logToCrashlytics()
        _uiState.update {
            it.copy(
                loading = false,
                showErrorDialog = true,
                errorMessage = ex.localizedMessage,
                criticalError = criticalError,
                accessRequestBusy = false,
                watchListBusy = false,
                markWatchedBusy = false
            )
        }
    }

    private fun hideError() {
        if(_uiState.value.criticalError) {
            popBackStack()
        }
        else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }

    private fun addDownload() {
        viewModelScope.launch {
            try{
                downloadManager.addMovie(_detailedMovie)
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun removeDownload() {
        viewModelScope.launch {
            try {
                downloadManager.delete(mediaId = mediaId, mediaType = MediaTypes.Movie)
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
     }


    private fun play() {
        navigateToRoute(
            PlayerNav.getRoute(
                mediaId =
                    if(_fromPlaylistDetails)
                        MediaCacheManager.Playlists[_detailedPlaylistCacheId]!!.id
                    else
                        MediaCacheManager.Movies[_detailCacheId]!!.id,
                sourceType =
                    if(_fromPlaylistDetails)
                        PlayerNav.MEDIA_TYPE_PLAYLIST
                    else
                        PlayerNav.MEDIA_TYPE_MOVIE,
                upNextId =
                    if(_fromPlaylistDetails)
                        _playlistUpNextIndex
                    else
                        0
            )
        )
    }

    private fun requestAccess() {
        _uiState.update {
            it.copy(accessRequestBusy = true)
        }

        viewModelScope.launch {
            try{
                mediaRepository.requestAccessOverride(mediaId)
                _uiState.update {
                    it.copy(
                        accessRequestBusy = false,
                        accessRequestStatus = OverrideRequestStatus.Requested
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun toggleWatchList() {
        _uiState.update {
            it.copy(watchListBusy = true)
        }
        viewModelScope.launch {

            try {
                if(_uiState.value.inWatchList) {
                    mediaRepository.deleteFromWatchlist(mediaId)
                } else {
                    mediaRepository.addToWatchlist(mediaId)
                }

                _uiState.update {
                    it.copy(
                        watchListBusy = false,
                        inWatchList = _uiState.value.inWatchList.not()
                    )
                }

                HomeViewModel.triggerUpdate()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun markWatched() {
        _uiState.update {
            it.copy(markWatchedBusy = true)
        }

        viewModelScope.launch {
            try{
                mediaRepository.updatePlaybackProgress(PlaybackProgress(id = mediaId, seconds = -1.0))
                _uiState.update {
                    it.copy(
                        markWatchedBusy = false,
                        partiallyPlayed = false
                    )
                }

                HomeViewModel.triggerUpdate()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun addToPlaylist() {
        navigateToRoute(AddToPlaylistNav.getRouteForId(mediaId, false))
    }

    private fun managePermissions() {
        navigateToRoute(ManageParentalControlsForTitleNav.getRouteForId(mediaId))
    }

    private fun genreNav(genrePair: GenrePair) {
        navigateToRoute(ShowMoreNav.getRoute(genrePair.genre.value, genrePair.text))
    }

    private fun personNav(id: Int){

    }
}




















