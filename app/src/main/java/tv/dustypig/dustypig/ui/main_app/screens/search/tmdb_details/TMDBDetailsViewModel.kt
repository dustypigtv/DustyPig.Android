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
import tv.dustypig.dustypig.api.models.Genres
import tv.dustypig.dustypig.api.models.RequestStatus
import tv.dustypig.dustypig.api.models.TMDBMediaTypes
import tv.dustypig.dustypig.api.models.TitleRequest
import tv.dustypig.dustypig.api.models.TitleRequestPermissions
import tv.dustypig.dustypig.api.repositories.FriendsRepository
import tv.dustypig.dustypig.api.repositories.ProfilesRepository
import tv.dustypig.dustypig.api.repositories.TMDBRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo
import javax.inject.Inject

@HiltViewModel
class TMDBDetailsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val friendsRepository: FriendsRepository,
    private val tmdbRepository: TMDBRepository,
    private val authManager: AuthManager,
    private val profilesRepository: ProfilesRepository,
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
                    tmdbRepository.getMovie(_tmdbId)
                else
                    tmdbRepository.getSeries(_tmdbId)


                val friendsForRequests = arrayListOf<TMDBDetailsRequestFriend>()
                if(_detailedTMDB.requestPermissions == TitleRequestPermissions.Enabled) {
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

                    if(!authManager.currentProfileIsMain) {
                        val detailedProfile = results[1] as DetailedProfile
                        friendsForRequests.add(
                            index =0,
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
                            cast = _detailedTMDB.cast,
                            directors = _detailedTMDB.directors,
                            producers = _detailedTMDB.producers,
                            writers = _detailedTMDB.writers
                        ),
                        rated = _detailedTMDB.rated ?: "",
                        year = if(_detailedTMDB.year > 1900) _detailedTMDB.year.toString() else "",
                        available = _detailedTMDB.available,
                        requestPermissions = _detailedTMDB.requestPermissions,
                        requestStatus = _detailedTMDB.requestStatus,
                        friends = friendsForRequests
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


    fun requestTitle(id: Int?) {
        _uiState.update {
            it.copy(
                busy = true
            )
        }

        viewModelScope.launch {
            try{
                tmdbRepository.requestTitle(titleRequest = TitleRequest(tmdbId = _tmdbId, friendId = id, mediaType = _detailedTMDB.mediaType))
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

    fun cancelRequest() {
        _uiState.update {
            it.copy(busy = true)
        }
        viewModelScope.launch {
            try{
                tmdbRepository.cancelTitleRequest(titleRequest = TitleRequest(tmdbId = _tmdbId, mediaType = _detailedTMDB.mediaType))
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
}