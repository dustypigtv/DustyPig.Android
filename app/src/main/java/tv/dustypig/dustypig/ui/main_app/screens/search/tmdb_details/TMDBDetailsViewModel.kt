package tv.dustypig.dustypig.ui.main_app.screens.search.tmdb_details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.DetailedTMDB
import tv.dustypig.dustypig.api.models.GenrePair
import tv.dustypig.dustypig.api.models.Genres
import tv.dustypig.dustypig.api.models.RequestStatus
import tv.dustypig.dustypig.api.models.TitleRequest
import tv.dustypig.dustypig.api.models.TitleRequestPermissions
import tv.dustypig.dustypig.api.repositories.FriendsRepository
import tv.dustypig.dustypig.api.repositories.ProfilesRepository
import tv.dustypig.dustypig.api.repositories.TMDBRepository
import tv.dustypig.dustypig.global_managers.ArtworkCache
import tv.dustypig.dustypig.global_managers.auth_manager.AuthManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.main_app.screens.show_more.ShowMoreNav
import javax.inject.Inject

@HiltViewModel
class TMDBDetailsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val friendsRepository: FriendsRepository,
    private val tmdbRepository: TMDBRepository,
    private val authManager: AuthManager,
    private val profilesRepository: ProfilesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), RouteNavigator by routeNavigator {

    companion object {
        private const val TAG = "TMDBDetailsViewModel"
    }

    private val _tmdbId: Int = savedStateHandle.getOrThrow(TMDBDetailsNav.KEY_MEDIA_ID)
    private val _isMovie: Boolean = savedStateHandle.getOrThrow(TMDBDetailsNav.KEY_IS_MOVIE)

    private val _cachedPoster =
        if(_isMovie)
            ArtworkCache.getTMDBMoviePoster(_tmdbId) ?: ""
        else
            ArtworkCache.getTMDBSeriesPoster(_tmdbId) ?: ""

    private val _cachedBackdrop =
        if(_isMovie)
            ArtworkCache.getTMDBMovieBackdrop(_tmdbId) ?: ""
        else
            ArtworkCache.getTMDBSeriesBackdrop(_tmdbId) ?: ""

    private val _uiState = MutableStateFlow(
        TMDBDetailsUIState(
            posterUrl = _cachedPoster,
            backdropUrl = _cachedBackdrop,
            isMovie = _isMovie,
            onPopBackStack = ::popBackStack,
            onHideError = ::hideErrorDialog,
            onCancelRequest = ::cancelRequest,
            onRequestTitle = ::requestTitle
        )
    )
    val uiState = _uiState.asStateFlow()

    private var _detailedTMDB: DetailedTMDB? = null

    init {

        viewModelScope.launch {
            try {

                _detailedTMDB = if (_isMovie)
                    tmdbRepository.getMovie(_tmdbId)
                else
                    tmdbRepository.getSeries(_tmdbId)


                val friendsForRequests = arrayListOf<TMDBDetailsRequestFriend>()
                if (_detailedTMDB!!.requestPermission == TitleRequestPermissions.Enabled) {

                    val friendsList = friendsRepository.list()
                    friendsList.forEach {
                        friendsForRequests.add(
                            TMDBDetailsRequestFriend(
                                id = it.id,
                                name = it.displayName,
                                avatarUrl = it.avatarUrl
                            )
                        )
                    }

                    if (!authManager.currentProfileIsMain) {
                        val detailedProfile = profilesRepository.getMainProfileDetails()
                        friendsForRequests.add(
                            index = 0,
                            element = TMDBDetailsRequestFriend(
                                id = null,
                                name = detailedProfile.name,
                                avatarUrl = detailedProfile.avatarUrl
                            )
                        )
                    }

                }

                if(_detailedTMDB!!.artworkUrl != _cachedPoster
                    || _detailedTMDB!!.backdropUrl != _cachedBackdrop) {
                    _uiState.update {
                        it.copy(
                            posterUrl = _detailedTMDB!!.artworkUrl ?: "",
                            backdropUrl = _detailedTMDB!!.backdropUrl ?: ""
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        loading = false,
                        title = _detailedTMDB!!.title,
                        overview = _detailedTMDB!!.description ?: "",
                        creditsData = CreditsData(
                            routeNavigator = routeNavigator,
                            genres = Genres(_detailedTMDB!!.genres).toList(),
                            genreNav = ::genreNav,
                            castAndCrew = _detailedTMDB!!.credits ?: listOf(),
                        ),
                        rated = _detailedTMDB!!.rated ?: "",
                        year = if (_detailedTMDB!!.year > 1900) _detailedTMDB!!.year.toString() else "",
                        available = _detailedTMDB!!.available ?: listOf(),
                        requestPermissions = _detailedTMDB!!.requestPermission,
                        requestStatus = _detailedTMDB!!.requestStatus,
                        friends = friendsForRequests
                    )
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Error getting TMDB details", ex)
                setError(ex = ex, criticalError = true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if(_isMovie) {
            ArtworkCache.deleteTMDBMovie(_tmdbId)
        } else {
            ArtworkCache.deleteTMDBSeries(_tmdbId)
        }
    }

    private fun setError(ex: Exception, criticalError: Boolean) {
        ex.logToCrashlytics()
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

    private fun hideErrorDialog() {
        if (_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }


    private fun requestTitle(friendId: Int?) {
        _uiState.update {
            it.copy(
                busy = true
            )
        }

        viewModelScope.launch {
            try {
                tmdbRepository.requestTitle(
                    titleRequest = TitleRequest(
                        tmdbId = _tmdbId,
                        friendId = friendId,
                        mediaType = _detailedTMDB!!.mediaType
                    )
                )
                _uiState.update {
                    it.copy(
                        busy = false,
                        requestStatus = RequestStatus.RequestSentToAccount
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun cancelRequest() {
        _uiState.update {
            it.copy(busy = true)
        }
        viewModelScope.launch {
            try {
                tmdbRepository.cancelTitleRequest(
                    titleRequest = TitleRequest(
                        tmdbId = _tmdbId,
                        mediaType = _detailedTMDB!!.mediaType
                    )
                )
                _uiState.update {
                    it.copy(
                        busy = false,
                        requestStatus = RequestStatus.NotRequested
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun genreNav(genrePair: GenrePair) {
        navigateToRoute(ShowMoreNav.getRoute(genrePair.genre.value, genrePair.text))
    }
}