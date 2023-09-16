package tv.dustypig.dustypig.ui.main_app.screens.tmdb_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.API
import tv.dustypig.dustypig.api.Genres
import tv.dustypig.dustypig.api.models.DetailedTMDB
import tv.dustypig.dustypig.api.models.RequestStatus
import tv.dustypig.dustypig.api.models.TMDB_MediaTypes
import tv.dustypig.dustypig.api.models.TitleRequest
import tv.dustypig.dustypig.api.models.TitleRequestPermissions
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo
import javax.inject.Inject

@HiltViewModel
class TMDBDetailsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(TMDBDetailsUIState())
    val uiState = _uiState.asStateFlow()

    private val _tmdbId: Int = savedStateHandle.getOrThrow(TMDBDetailsNav.KEY_ID)
    private val _isMovie: Boolean = savedStateHandle.getOrThrow(TMDBDetailsNav.KEY_IS_MOVIE)
    private lateinit var _detailedTMDB: DetailedTMDB

    init {
        _uiState.update {
            it.copy(
                loading = true,
                posterUrl = ScreenLoadingInfo.posterUrl,
                backdropUrl = ScreenLoadingInfo.backdropUrl,
                title = ScreenLoadingInfo.title,
                isMovie = _isMovie
            )
        }
        viewModelScope.launch {
            try {
                _detailedTMDB = if(_isMovie)
                    API.TMDB.getMovie(_tmdbId)
                else
                    API.TMDB.getSeries(_tmdbId)

                 _uiState.update {
                    it.copy(
                        loading = false,
                        isMovie = _detailedTMDB.mediaType == TMDB_MediaTypes.Movie,
                        title = _detailedTMDB.title,
                        posterUrl = _detailedTMDB.artworkUrl ?: "",
                        backdropUrl = _detailedTMDB.backdropUrl ?: "",
                        overview = _detailedTMDB.description ?: "",
                        creditsData = CreditsData(
                            genres = Genres(_detailedTMDB.genres).toList(),
                            cast = _detailedTMDB.cast ?: listOf(),
                            directors = _detailedTMDB.directors ?: listOf(),
                            producers = _detailedTMDB.producers ?: listOf(),
                            writers = _detailedTMDB.writers ?: listOf()
                        ),
                        rated = _detailedTMDB.rated ?: "",
                        year = if(_detailedTMDB.year > 1900) _detailedTMDB.year.toString() else "",
                        available = _detailedTMDB.available ?: listOf(),
                        requestPermissions = _detailedTMDB.requestPermissions ?: TitleRequestPermissions.Enabled,
                        requestStatus = _detailedTMDB.requestStatus ?: RequestStatus.NotRequested
                    )
                }
            } catch (ex: Exception) {
                showErrorDialog(ex = ex, criticalError = true)
            }
        }
    }

    private fun showErrorDialog(ex: Exception, criticalError: Boolean) {
        _uiState.update {
            it.copy(
                loading = false,
                busy = false,
                showErrorDialog = true,
                errorMessage = ex.localizedMessage,
                criticalError = criticalError
            )
        }
    }

    fun hideErrorDialog() {
        if(_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }

    fun requestTitle() {

        _uiState.update {
            it.copy(busy = true)
        }

        if(_detailedTMDB.requestPermissions == TitleRequestPermissions.RequiresAuthorization) {

            viewModelScope.launch {
                try {
                    //API.TMDB.requestTMDBTitle(titleRequest = TitleRequest(tmdbId = _tmdbId, mediaType = _detailedTMDB.mediaType))
                    API.TMDB.requestTitle(titleRequest = TitleRequest(tmdbId = _tmdbId, mediaType = _detailedTMDB.mediaType))
                    _uiState.update {
                        it.copy(
                            busy = false,
                            requestStatus = RequestStatus.RequestSentToMain
                        )
                    }
                } catch (ex: Exception) {
                    showErrorDialog(ex = ex, criticalError = false)
                }
            }

        } else {
            viewModelScope.launch {
                try {
                    val friends = API.Friends.list()
                    _uiState.update {
                        it.copy(
                            showFriendsDialog = true,
                            friends = friends
                        )
                    }
                } catch (ex: Exception) {
                    showErrorDialog(ex = ex, criticalError = false)
                }
            }
        }
    }

    fun hideFriendsDialog(friendId: Int) {
        _uiState.update {
            it.copy(
                busy = friendId >= 0,
                showFriendsDialog = false
            )
        }
        if(friendId < 0)
            return

        viewModelScope.launch {
            try{
                //API.TMDB.requestTMDBTitle(titleRequest = TitleRequest(tmdbId = _tmdbId, friendId = friendId, mediaType = _detailedTMDB.mediaType))
                API.TMDB.requestTitle(titleRequest = TitleRequest(tmdbId = _tmdbId, friendId = friendId, mediaType = _detailedTMDB.mediaType))
                _uiState.update {
                    it.copy(
                        busy = false,
                        requestStatus = RequestStatus.RequestSentToAccount
                    )
                }
            } catch (ex: Exception) {
                showErrorDialog(ex = ex, criticalError = false)
            }
        }
    }

    fun cancelRequest() {
        _uiState.update {
            it.copy(busy = true)
        }
        viewModelScope.launch {
            try{
                //API.TMDB.cancelTMDBTitleRequest(titleRequest = TitleRequest(tmdbId = _tmdbId, mediaType = _detailedTMDB.mediaType))
                API.TMDB.cancelTitleRequest(titleRequest = TitleRequest(tmdbId = _tmdbId, mediaType = _detailedTMDB.mediaType))
                _uiState.update {
                    it.copy(
                        busy = false,
                        requestStatus = RequestStatus.NotRequested
                    )
                }
            } catch (ex: Exception) {
                showErrorDialog(ex = ex, criticalError = false)
            }
        }
    }
}