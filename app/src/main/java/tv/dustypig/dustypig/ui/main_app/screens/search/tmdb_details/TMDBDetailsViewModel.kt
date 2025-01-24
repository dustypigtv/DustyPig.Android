package tv.dustypig.dustypig.ui.main_app.screens.search.tmdb_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.BasicFriend
import tv.dustypig.dustypig.api.models.DetailedProfile
import tv.dustypig.dustypig.api.models.DetailedTMDB
import tv.dustypig.dustypig.api.models.GenrePair
import tv.dustypig.dustypig.api.models.Genres
import tv.dustypig.dustypig.api.models.RequestStatus
import tv.dustypig.dustypig.api.models.TMDBMediaTypes
import tv.dustypig.dustypig.api.models.TitleRequest
import tv.dustypig.dustypig.api.models.TitleRequestPermissions
import tv.dustypig.dustypig.api.repositories.FriendsRepository
import tv.dustypig.dustypig.api.repositories.ProfilesRepository
import tv.dustypig.dustypig.api.repositories.TMDBRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.global_managers.media_cache_manager.MediaCacheManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.main_app.screens.person_details.PersonDetailsNav
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

    private val _uiState = MutableStateFlow(
        TMDBDetailsUIState(
            onPopBackStack = ::popBackStack,
            onHideError = ::hideErrorDialog,
            onCancelRequest = ::cancelRequest,
            onRequestTitle = ::requestTitle
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _cacheId: String = savedStateHandle.getOrThrow(TMDBDetailsNav.KEY_CACHE_ID)
    private val _tmdbId: Int = savedStateHandle.getOrThrow(TMDBDetailsNav.KEY_MEDIA_ID)
    private val _isMovie: Boolean = savedStateHandle.getOrThrow(TMDBDetailsNav.KEY_IS_MOVIE)
    private lateinit var _detailedTMDB: DetailedTMDB

    init {
        val cachedInfo = MediaCacheManager.getBasicInfo(_cacheId)
        _uiState.update {
            it.copy(
                loading = true,
                posterUrl = cachedInfo.posterUrl,
                backdropUrl = cachedInfo.backdropUrl ?: "",
                title = cachedInfo.title,
                isMovie = _isMovie
            )
        }
        viewModelScope.launch {
            try {

                _detailedTMDB = if (_isMovie)
                    tmdbRepository.getMovie(_tmdbId)
                else
                    tmdbRepository.getSeries(_tmdbId)


                val friendsForRequests = arrayListOf<TMDBDetailsRequestFriend>()
                if (_detailedTMDB.requestPermission == TitleRequestPermissions.Enabled) {
                    val calls = arrayListOf<Deferred<*>>()
                    calls.add(async { friendsRepository.list() })
                    if (!authManager.currentProfileIsMain) {
                        calls.add(async { profilesRepository.details(authManager.currentProfileId) })
                    }
                    val results = calls.awaitAll()


                    @Suppress("UNCHECKED_CAST")
                    val friendsList = results[0] as List<BasicFriend>
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
                        val detailedProfile = results[1] as DetailedProfile
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


                _uiState.update {
                    it.copy(
                        loading = false,
                        isMovie = _detailedTMDB.mediaType == TMDBMediaTypes.Movie,
                        title = _detailedTMDB.title,
                        overview = _detailedTMDB.description ?: "",
                        creditsData = CreditsData(
                            genres = Genres(_detailedTMDB.genres).toList(),
                            genreNav = ::genreNav,
                            castAndCrew = _detailedTMDB.credits ?: listOf(),
                            personNav = ::personNav
                        ),
                        rated = _detailedTMDB.rated ?: "",
                        year = if (_detailedTMDB.year > 1900) _detailedTMDB.year.toString() else "",
                        available = _detailedTMDB.available ?: listOf(),
                        requestPermissions = _detailedTMDB.requestPermission,
                        requestStatus = _detailedTMDB.requestStatus,
                        friends = friendsForRequests
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        MediaCacheManager.BasicInfo.removeAll { it.cacheId == _cacheId }
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
                        mediaType = _detailedTMDB.mediaType
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
                        mediaType = _detailedTMDB.mediaType
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

    private fun personNav(tmdbId: Int, cacheId: String) {
        navigateToRoute(PersonDetailsNav.getRoute(tmdbId, cacheId))
    }
}