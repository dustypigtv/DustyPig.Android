package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.ThePig
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(MovieDetailsUIState())
    val uiState: StateFlow<MovieDetailsUIState> = _uiState.asStateFlow()

    private var _id: Int = ThePig.selectedBasicMedia.id
    private lateinit var _detailedMovie: DetailedMovie

    init {
        //_id = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_ID)

        _uiState.update {
            it.copy(
                loading = true,
                isPoster = true,
                artworkUrl = ThePig.selectedBasicMedia.artworkUrl
            )
        }

        viewModelScope.launch {
            try {
                _detailedMovie = ThePig.Api.Movies.movieDetails(_id)
                _uiState.update {
                    it.copy(
                        loading = false,
                        detailedMovie = _detailedMovie,
                        inWatchList = _detailedMovie.inWatchlist,
                        isPoster = _detailedMovie.backdropUrl.isNullOrBlank(),
                        artworkUrl = if(_detailedMovie.backdropUrl.isNullOrBlank()) _detailedMovie.artworkUrl else _detailedMovie.backdropUrl!!
                    )
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }


}