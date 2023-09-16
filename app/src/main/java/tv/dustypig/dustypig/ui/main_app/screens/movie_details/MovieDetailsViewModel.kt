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
import tv.dustypig.dustypig.api.API
import tv.dustypig.dustypig.api.Genres
import tv.dustypig.dustypig.api.asString
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.api.toTimeString
import tv.dustypig.dustypig.download_manager.DownloadManager
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
    private val savedStateHandle: SavedStateHandle
): DetailsScreenBaseViewModel(routeNavigator) {

    private val _uiState = MutableStateFlow(MovieDetailsUIState())
    val uiState: StateFlow<MovieDetailsUIState> = _uiState.asStateFlow()

    private val _titleInfoUIState = getTitleInfoUIStateForUpdate()

    override val mediaId: Int = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_ID)
    private lateinit var _detailedMovie: DetailedMovie

    init {

        _titleInfoUIState.update {
            it.copy(title = ScreenLoadingInfo.title)
        }

        _uiState.update {
            it.copy(
                posterUrl = ScreenLoadingInfo.posterUrl,
                backdropUrl = ScreenLoadingInfo.backdropUrl
            )
        }

        viewModelScope.launch {
            try {
                _detailedMovie = API.Movies.movieDetails(mediaId)
                _uiState.update {
                    it.copy(
                        loading = false,
                        posterUrl = _detailedMovie.artworkUrl,
                        backdropUrl = _detailedMovie.backdropUrl ?: "",
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

                _titleInfoUIState.update { it ->
                    it.copy(
                        playClick = { play() },
                        toggleWatchList = { toggleWatchList() },
                        download = { toggleDownload() },
                        addToPlaylist = { addToPlaylist() },
                        markWatched = { markWatched() },
                        requestAccess = { requestAccess() },
                        manageClick = { manageParentalControls() },
                        inWatchList = _detailedMovie.inWatchlist,
                        title = _detailedMovie.title,
                        year = SimpleDateFormat("yyyy").format(_detailedMovie.date),
                        canManage = _detailedMovie.canManage,
                        canPlay = _detailedMovie.canPlay,
                        rated = _detailedMovie.rated.asString(),
                        length = _detailedMovie.length.toTimeString(),
                        partiallyPlayed = (_detailedMovie.played ?: 0.0) > 0.0,
                        overview = _detailedMovie.description ?: "",
                        accessRequestStatus = _detailedMovie.accessRequestStatus,
                        accessRequestBusy = false,
                        mediaId = mediaId
                    )
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error",
                        criticalError = true
                    )
                }
            }
        }
    }

    fun hideError() {
        if(_uiState.value.criticalError) {
            popBackStack()
        }
        else {
            _uiState.update {
                it.copy(showError = false)
            }
        }
    }

    fun hideDownload(confirmed: Boolean) {
        if(confirmed) {
            viewModelScope.launch {
                DownloadManager.delete(id = mediaId)
            }
            _uiState.update {
                it.copy(
                    showRemoveDownload = false
                )
            }
        } else {
            _uiState.update {
                it.copy(showRemoveDownload = false)
            }
        }
    }


    private fun play() {
        navigateToRoute(PlayerNav.getRouteForId(mediaId))
    }

    private fun toggleDownload() {

        viewModelScope.launch {
            if (DownloadManager.getJobCount(mediaId) > 0) {
                _uiState.update {
                    it.copy(showRemoveDownload = true)
                }
            } else {
                DownloadManager.addMovie(_detailedMovie)
            }
        }
    }

    private fun requestAccess() {
        _titleInfoUIState.update {
            it.copy(accessRequestBusy = true)
        }

        viewModelScope.launch {
            try{
                API.Media.requestAccessOverride(mediaId)
                _titleInfoUIState.update {
                    it.copy(
                        accessRequestBusy = false,
                        accessRequestStatus = OverrideRequestStatus.Requested
                    )
                }
            } catch (ex: Exception) {
                _titleInfoUIState.update {
                    it.copy(accessRequestBusy = false)
                }

                _uiState.update {
                    it.copy(
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }

    private fun toggleWatchList() {
        _titleInfoUIState.update {
            it.copy(watchListBusy = true)
        }

        viewModelScope.launch {

            try {

                if(_titleInfoUIState.value.inWatchList) {
                    API.Media.deleteFromWatchlist(mediaId)
                } else {
                    API.Media.addToWatchlist(mediaId)
                }

                _titleInfoUIState.update {
                    it.copy(
                        watchListBusy = false,
                        inWatchList = _titleInfoUIState.value.inWatchList.not()
                    )
                }

                HomeViewModel.triggerUpdate()

            } catch (ex: Exception) {
                _titleInfoUIState.update {
                    it.copy(watchListBusy = false)
                }
                _uiState.update{
                    it.copy(
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }

    private fun markWatched() {

        _titleInfoUIState.update {
            it.copy(markWatchedBusy = true)
        }

        viewModelScope.launch {
            try{
                API.Media.updatePlaybackProgress(id = mediaId, seconds = -1.0)
                _titleInfoUIState.update {
                    it.copy(
                        markWatchedBusy = false,
                        partiallyPlayed = false
                    )
                }

                HomeViewModel.triggerUpdate()
            } catch (ex: Exception) {
                _titleInfoUIState.update {
                    it.copy(markWatchedBusy = false)
                }
                _uiState.update {
                    it.copy(
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
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