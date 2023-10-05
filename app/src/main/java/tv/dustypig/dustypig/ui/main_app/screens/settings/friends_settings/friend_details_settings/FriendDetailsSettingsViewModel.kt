package tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.friend_details_settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.BasicLibrary
import tv.dustypig.dustypig.api.models.DetailedFriend
import tv.dustypig.dustypig.api.models.UpdateFriend
import tv.dustypig.dustypig.api.repositories.FriendsRepository
import tv.dustypig.dustypig.api.repositories.LibrariesRepository
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import javax.inject.Inject

@HiltViewModel
class FriendDetailsSettingsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle,
    private val friendsRepository: FriendsRepository,
    private val librariesRepository: LibrariesRepository
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(FriendDetailsSettingsUIState())
    val uiState = _uiState.asStateFlow()

    private val _friendshipId: Int = savedStateHandle.getOrThrow(FriendDetailsSettingsNav.KEY_ID)
    private lateinit var _detailedFriend: DetailedFriend
    private lateinit var _myLibs: List<BasicLibrary>

    init {
        viewModelScope.launch {
            try{
                val deferredMyLibs = async { librariesRepository.adminList() }
                val deferredDetailedFriend = async { friendsRepository.details(_friendshipId) }
                val lst = awaitAll(deferredMyLibs, deferredDetailedFriend)

                @Suppress("UNCHECKED_CAST")
                _myLibs = lst[0] as List<BasicLibrary>
                _detailedFriend = lst[1] as DetailedFriend

                val transformedList: ArrayList<ShareableLibrary> = arrayListOf()
                for(lib in _myLibs) {
                    transformedList.add(
                        ShareableLibrary(
                            id = lib.id,
                            name = lib.name,
                            isTV = lib.isTV,
                            shared = _detailedFriend.libsSharedWithFriend.any {
                                it.id == lib.id
                            }
                        )
                    )
                }

                _uiState.update {
                    it.copy(
                        busy = false,
                        displayName = _detailedFriend.displayName,
                        avatarUrl = _detailedFriend.avatarUrl,
                        libsSharedWithMe = _detailedFriend.libsSharedWithMe,
                        myLibs = transformedList
                    )
                }

            } catch (ex: Exception) {
                setError(ex = ex, critical = true)
            }
        }
    }

    private fun setError(ex: Exception, critical: Boolean) {
        ex.logToCrashlytics()
        _uiState.update {
            it.copy(
                busy = false,
                showError = true,
                errorMessage = ex.localizedMessage,
                criticalError = critical
            )
        }
    }

    fun hideError() {
        if(_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showError = false)
            }
        }
    }

    fun changeDisplayName(newName: String) {
        _uiState.update {
            it.copy(busy = true)
        }

        try {
            val trimmedNewName = newName.trim()
            val updateFriend = UpdateFriend(
                id = _detailedFriend.id,
                displayName = trimmedNewName,
                accepted = _detailedFriend.accepted
            )
            _uiState.update {
                it.copy(
                    busy = false,
                    displayName = trimmedNewName
                )
            }
        } catch (ex: Exception) {
            setError(ex = ex, critical = false)
        }
    }
}