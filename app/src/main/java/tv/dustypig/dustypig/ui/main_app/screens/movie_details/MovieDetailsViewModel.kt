package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.Genres
import tv.dustypig.dustypig.api.ThePig
import tv.dustypig.dustypig.api.asString
import tv.dustypig.dustypig.api.toTimeString
import tv.dustypig.dustypig.nav.RouteNavigator
import java.text.SimpleDateFormat
import javax.inject.Inject

@SuppressLint("SimpleDateFormat")
@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    //private val savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(MovieDetailsUIState())
    val uiState: StateFlow<MovieDetailsUIState> = _uiState.asStateFlow()

//    private var _id: Int = ThePig.selectedBasicMedia.id
//    private lateinit var _detailedMovie: DetailedMovie

    init {
        //_id = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_ID)

        _uiState.update {
            it.copy(
                loading = true,
                posterUrl = ThePig.selectedBasicMedia.artworkUrl
            )
        }



        viewModelScope.launch {
            try {
                val data = ThePig.Api.Movies.movieDetails(ThePig.selectedBasicMedia.id)

                _uiState.update {
                    it.copy(
                        loading = false,
                        inWatchList = data.inWatchlist,
                        posterUrl = data.artworkUrl,
                        backdropUrl = if(data.backdropUrl.isNullOrBlank()) data.artworkUrl else data.backdropUrl!!,
                        title = data.title,
                        year = SimpleDateFormat("yyyy").format(data.date),
                        canManage = data.canManage,
                        canPlay = data.canPlay,
                        rated = data.rated.asString(),
                        length = data.length.toTimeString(),
                        partiallyPlayed = (data.played ?: 0.0) > 0.0,
                        description = data.description ?: "",
                        genres = Genres(data.genres).toList(),
                        cast = data.cast ?: listOf(),
                        directors = data.directors ?: listOf(),
                        producers = data.producers ?: listOf(),
                        writers = data.writers ?: listOf(),
                        owner = data.owner ?: ""
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

    fun play() {

    }

    fun download() {

    }

    fun requestAccess() {

    }

    fun toggleWatchList() {

    }

    fun markWatched() {

    }

    fun addToPlaylist() {

    }

    fun manageParentalControls() {

    }
}