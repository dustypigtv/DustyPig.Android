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
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.toTimeString
import tv.dustypig.dustypig.download_manager.DownloadManager
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.download_manager.DownloadStatus
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

    private val _id: Int = ThePig.selectedBasicMedia.id

    init {
        //_id = savedStateHandle.getOrThrow(MovieDetailsNav.KEY_ID)

        _uiState.update {
            it.copy(
                loading = true,
                title = ThePig.selectedBasicMedia.title,
                posterUrl = ThePig.selectedBasicMedia.artworkUrl
            )
        }



        viewModelScope.launch {
            try {
                val data = ThePig.Api.Movies.movieDetails(_id)

                _uiState.update {
                    it.copy(
                        loading = false,
                        inWatchList = data.inWatchlist,
                        posterUrl = data.artworkUrl,
                        backdropUrl = data.backdropUrl ?: "",
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
                        errorMessage = ex.localizedMessage ?: "Unknown Error",
                        criticalError = true
                    )
                }
            }
        }
    }

    fun hideError(critical: Boolean) {
        if(critical) {
            popBackStack()
        }
        else {
            _uiState.update {
                it.copy(showError = false)
            }
        }
    }


    fun play() {
        TODO()
    }

    fun toggleDownload() {
        if(DownloadManager.has(_id)) {
            _uiState.update {
                it.copy(showRemoveDownload = true)
            }
        } else {
            DownloadManager.add(id = _id, mediaType = MediaTypes.Movie, count = 1)
            _uiState.update {
                it.copy(downloadStatus = DownloadStatus.Queued)
            }
        }
    }

    fun hideDownload(confirmed: Boolean) {
        if(confirmed) {
            DownloadManager.delete(id = _id)
            _uiState.update {
                it.copy(
                    showRemoveDownload = false,
                    downloadStatus = DownloadStatus.NotDownloaded
                )
            }
        } else {
            _uiState.update {
                it.copy(showRemoveDownload = false)
            }
        }
    }

    fun requestAccess() {
        TODO()
    }

    fun toggleWatchList() {
        _uiState.update {
            it.copy(watchListBusy = true)
        }

        viewModelScope.launch {

            try {

                if(_uiState.value.inWatchList) {
                    ThePig.Api.Media.deleteFromWatchlist(_id)
                } else {
                    ThePig.Api.Media.addToWatchlist(_id)
                }

                _uiState.update {
                    it.copy(
                        watchListBusy = false,
                        inWatchList = _uiState.value.inWatchList.not()
                    )
                }
            } catch (ex: Exception) {
                _uiState.update{
                    it.copy(
                        watchListBusy = false,
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }

    fun markWatched() {

        _uiState.update {
            it.copy(markWatchedBusy = true)
        }

        viewModelScope.launch {
            try{
                ThePig.Api.Media.updatePlaybackProgress(id = _id, seconds = -1.0)
                _uiState.update {
                    it.copy(
                        markWatchedBusy = false,
                        partiallyPlayed = false
                    )
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        markWatchedBusy = false,
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }

    fun addToPlaylist() {
        TODO()
    }

    fun manageParentalControls() {
        TODO()
    }
}