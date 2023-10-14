package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.Genres
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.api.repositories.MoviesRepository
import tv.dustypig.dustypig.api.toTimeString
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.main_app.DetailsScreenBaseViewModel
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title.ManageParentalControlsForTitleNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import java.text.SimpleDateFormat
import javax.inject.Inject





@SuppressLint("SimpleDateFormat")
@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle,
    private val mediaRepository: MediaRepository,
    private val moviesRepository: MoviesRepository,
    downloadManager: DownloadManager
): DetailsScreenBaseViewModel(routeNavigator, downloadManager, MediaTypes.Movie) {

    private val _uiState = MutableStateFlow(MovieDetailsUIState())
    val uiState: StateFlow<MovieDetailsUIState> = _uiState.asStateFlow()

    override val mediaId: Int = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_ID)
    private lateinit var _detailedMovie: DetailedMovie

    init {

        baseTitleInfoUIState.update {
            it.copy(
                title = ScreenLoadingInfo.title,
                mediaType = MediaTypes.Movie,
                playClick = ::play,
                toggleWatchList = ::toggleWatchList,
                addDownload = ::addDownload,
                removeDownload = ::removeDownload,
                addToPlaylist = ::addToPlaylist,
                markMovieWatched = ::markWatched,
                requestAccess = ::requestAccess,
                manageClick = ::manageParentalControls
            )
        }

        _uiState.update {
            it.copy(
                posterUrl = ScreenLoadingInfo.posterUrl,
                backdropUrl = ScreenLoadingInfo.backdropUrl
            )
        }


        viewModelScope.launch {
            try {
                _detailedMovie = moviesRepository.details(mediaId)
                _uiState.update {
                    it.copy(
                        loading = false,
                        creditsData = CreditsData(
                            genres = Genres(_detailedMovie.genres).toList(),
                            cast = _detailedMovie.cast ?: listOf(),
                            directors = _detailedMovie.directors ?: listOf(),
                            producers = _detailedMovie.producers ?: listOf(),
                            writers = _detailedMovie.writers ?: listOf(),
                            owner = _detailedMovie.owner ?: ""
                        )
                    )
                }

                // Prevent flicker by only updating if needed
                if(_detailedMovie.artworkUrl != _uiState.value.posterUrl || _detailedMovie.backdropUrl != _uiState.value.backdropUrl) {
                    _uiState.update {
                        it.copy(
                            posterUrl = _detailedMovie.artworkUrl,
                            backdropUrl = _detailedMovie.backdropUrl ?: ""
                        )
                    }
                }

                baseTitleInfoUIState.update {
                    it.copy(
                        inWatchList = _detailedMovie.inWatchlist,
                        title = _detailedMovie.title,
                        year = SimpleDateFormat("yyyy").format(_detailedMovie.date),
                        canManage = _detailedMovie.canManage,
                        canPlay = _detailedMovie.canPlay,
                        rated = _detailedMovie.rated.toString(),
                        length = _detailedMovie.length.toTimeString(),
                        partiallyPlayed = (_detailedMovie.played ?: 0.0) > 0.0,
                        overview = _detailedMovie.description ?: "",
                        titleRequestPermissions = _detailedMovie.titleRequestPermissions,
                        accessRequestStatus = _detailedMovie.accessRequestStatus,
                        accessRequestBusy = false,
                        mediaId = mediaId
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = true)
            }
        }

    }

    private fun setError(ex: Exception, criticalError: Boolean) {
        ex.logToCrashlytics()
        _uiState.update {
            it.copy(
                loading = false,
                showErrorDialog = true,
                errorMessage = ex.localizedMessage,
                criticalError = criticalError
            )
        }
        baseTitleInfoUIState.update {
            it.copy(
                accessRequestBusy = false,
                watchListBusy = false,
                markWatchedBusy = false
            )
        }
    }

    fun hideError() {
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
        navigateToRoute(PlayerNav.getRouteForId(mediaId))
    }


    private fun requestAccess() {
        baseTitleInfoUIState.update {
            it.copy(accessRequestBusy = true)
        }

        viewModelScope.launch {
            try{
                mediaRepository.requestAccessOverride(mediaId)
                baseTitleInfoUIState.update {
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
        baseTitleInfoUIState.update {
            it.copy(watchListBusy = true)
        }

        viewModelScope.launch {

            try {

                if(baseTitleInfoUIState.value.inWatchList) {
                    mediaRepository.deleteFromWatchlist(mediaId)
                } else {
                    mediaRepository.addToWatchlist(mediaId)
                }

                baseTitleInfoUIState.update {
                    it.copy(
                        watchListBusy = false,
                        inWatchList = baseTitleInfoUIState.value.inWatchList.not()
                    )
                }

                HomeViewModel.triggerUpdate()

            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun markWatched() {

        baseTitleInfoUIState.update {
            it.copy(markWatchedBusy = true)
        }

        viewModelScope.launch {
            try{
                mediaRepository.updatePlaybackProgress(PlaybackProgress(id = mediaId, seconds = -1.0))
                baseTitleInfoUIState.update {
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

    private fun manageParentalControls() {
        navigateToRoute(ManageParentalControlsForTitleNav.getRouteForId(mediaId))
    }
}