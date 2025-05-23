package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import android.annotation.SuppressLint
import android.util.Log
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
import tv.dustypig.dustypig.global_managers.ArtworkCache
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.download_manager.MyDownloadManager
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
import javax.inject.Inject

@SuppressLint("SimpleDateFormat")
@HiltViewModel
@OptIn(UnstableApi::class)
class MovieDetailsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle,
    castManager: CastManager,
    private val mediaRepository: MediaRepository,
    private val moviesRepository: MoviesRepository,
    private val downloadManager: MyDownloadManager
) : ViewModel(), RouteNavigator by routeNavigator {

    companion object {
        private const val TAG = "MovieDetailsViewModel"
    }

    private val _mediaId: Int = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_MEDIA_ID)
    private val _detailedPlaylistId: Int = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_DETAILED_PLAYLIST_ID)
    private val _fromPlaylistDetails: Boolean = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_FROM_PLAYLIST_ID)
    private val _playlistUpNextIndex: Int = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_PLAYLIST_UPNEXT_INDEX_ID)

    private val _cachedPoster = ArtworkCache.getMediaPoster(_mediaId) ?: ""
    private val _cachedBackdrop = ArtworkCache.getMediaBackdrop(_mediaId) ?: ""

    private val _uiState = MutableStateFlow(
        MovieDetailsUIState(
            posterUrl = _cachedPoster,
            backdropUrl = _cachedBackdrop,
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



    private var _detailedMovie: DetailedMovie? = null


    init {
        viewModelScope.launch {
            PlayerStateManager.playbackEnded.collectLatest {
                updateData()
            }
        }

        viewModelScope.launch {
            downloadManager.currentDownloads.collectLatest { jobLst ->

                var status = DownloadStatus.None
                var percent = 0f
                var downloadingForPlaylist = false
                for(job in jobLst) {
                    for(dl in job.downloads) {
                        if(dl.mediaId == _mediaId) {
                            percent = dl.percent
                            status = dl.status
                            if(job.mediaType == MediaTypes.Playlist)
                                downloadingForPlaylist = true
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        downloadStatus = status,
                        downloadPercent = percent,
                        downloadingForPlaylist = downloadingForPlaylist
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ArtworkCache.deleteMedia(_mediaId)
    }

    private suspend fun updateData() {

        Log.d(TAG, "Updating data")

        try {
            _detailedMovie = moviesRepository.details(_mediaId)


            // Prevent flicker by only updating if needed
            if (_detailedMovie!!.artworkUrl != _cachedPoster ||
                _detailedMovie!!.backdropUrl != _cachedBackdrop) {
                _uiState.update {
                    it.copy(
                        posterUrl = _detailedMovie!!.artworkUrl,
                        backdropUrl = _detailedMovie!!.backdropUrl ?: ""
                    )
                }
            }

            val cal = Calendar.getInstance()
            cal.time = _detailedMovie!!.date

            _uiState.update {
                it.copy(
                    inWatchList = _detailedMovie!!.inWatchlist,
                    title = _detailedMovie!!.title,
                    year = cal.get(Calendar.YEAR).toString(),
                    canManage = _detailedMovie!!.canManage,
                    canPlay = _detailedMovie!!.canPlay,
                    rated = _detailedMovie!!.rated.toString(),
                    length = _detailedMovie!!.length.toTimeString(),
                    partiallyPlayed = (_detailedMovie!!.played ?: 0.0) > 0.0,
                    overview = _detailedMovie!!.description ?: "",
                    titleRequestPermissions = _detailedMovie!!.titleRequestPermission,
                    accessRequestStatus = _detailedMovie!!.accessRequestedStatus,
                    accessRequestBusy = false,
                    loading = false,
                    creditsData = CreditsData(
                        genres = Genres(_detailedMovie!!.genres).toList(),
                        genreNav = ::genreNav,
                        castAndCrew = _detailedMovie!!.credits ?: listOf(),
                        routeNavigator = this,
                        owner = _detailedMovie!!.owner ?: ""
                    )
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
            try {
                downloadManager.addMovie(_detailedMovie!!)
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun removeDownload() {
        viewModelScope.launch {
            try {
                downloadManager.delete(mediaId = _mediaId, mediaType = MediaTypes.Movie)
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }


    private fun play() {
        navigateToRoute(
            PlayerNav.getRoute(
                mediaId =
                    if (_fromPlaylistDetails)
                        _detailedPlaylistId
                    else
                        _detailedMovie!!.id,
                sourceType =
                    if (_fromPlaylistDetails)
                        PlayerNav.MEDIA_TYPE_PLAYLIST
                    else
                        PlayerNav.MEDIA_TYPE_MOVIE,
                upNextId =
                    if (_fromPlaylistDetails)
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
            try {
                mediaRepository.requestAccessOverride(_mediaId)
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
                if (_uiState.value.inWatchList) {
                    mediaRepository.deleteFromWatchlist(_mediaId)
                } else {
                    mediaRepository.addToWatchlist(_mediaId)
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
            try {
                mediaRepository.updatePlaybackProgress(
                    PlaybackProgress(id = _mediaId)
                )
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
        navigateToRoute(AddToPlaylistNav.getRouteForId(_mediaId, false))
    }

    private fun managePermissions() {
        navigateToRoute(ManageParentalControlsForTitleNav.getRouteForId(_mediaId))
    }

    private fun genreNav(genrePair: GenrePair) {
        navigateToRoute(ShowMoreNav.getRoute(genrePair.genre.value, genrePair.text))
    }
}
