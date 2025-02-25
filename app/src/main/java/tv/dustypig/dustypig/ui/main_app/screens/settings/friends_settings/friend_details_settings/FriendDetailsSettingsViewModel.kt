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
import tv.dustypig.dustypig.api.models.DetailedFriend
import tv.dustypig.dustypig.api.models.DetailedLibrary
import tv.dustypig.dustypig.api.models.LibraryFriendLink
import tv.dustypig.dustypig.api.models.UpdateFriend
import tv.dustypig.dustypig.api.repositories.FriendsRepository
import tv.dustypig.dustypig.api.repositories.LibrariesRepository
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.FriendsSettingsViewModel
import javax.inject.Inject

@HiltViewModel
class FriendDetailsSettingsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle,
    private val friendsRepository: FriendsRepository,
    private val librariesRepository: LibrariesRepository
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        FriendDetailsSettingsUIState(
            onPopBackStack = ::popBackStack,
            onHideError = ::hideError,
            onChangeDisplayName = ::changeDisplayName,
            onToggleLibraryShare = ::toggleLibraryShare,
            onUnfriend = ::unfriend
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _friendshipId: Int = savedStateHandle.getOrThrow(FriendDetailsSettingsNav.KEY_ID)
    private lateinit var _detailedFriend: DetailedFriend
    private lateinit var _myLibs: List<DetailedLibrary>

    init {
        viewModelScope.launch {
            try {
                val deferredMyLibs = async { librariesRepository.adminList() }
                val deferredDetailedFriend = async { friendsRepository.details(_friendshipId) }
                val lst = awaitAll(deferredMyLibs, deferredDetailedFriend)

                @Suppress("UNCHECKED_CAST")
                _myLibs = lst[0] as List<DetailedLibrary>
                _detailedFriend = lst[1] as DetailedFriend

                val transformedList: ArrayList<ShareableLibrary> = arrayListOf()
                for (lib in _myLibs) {
                    transformedList.add(
                        ShareableLibrary(
                            id = lib.id,
                            name = lib.name,
                            isTV = lib.isTV,
                            shared = _detailedFriend.sharedWithFriend.any {
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
                        libsSharedWithMe = _detailedFriend.sharedWithMe,
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

    private fun hideError() {
        if (_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showError = false)
            }
        }
    }

    private fun changeDisplayName(newName: String) {
        _uiState.update {
            it.copy(busy = true)
        }

        viewModelScope.launch {
            try {
                val trimmedNewName = newName.trim()
                val updateFriend = UpdateFriend(
                    id = _detailedFriend.id,
                    displayName = trimmedNewName,
                    accepted = _detailedFriend.accepted
                )
                friendsRepository.update(updateFriend)

                _uiState.update {
                    it.copy(
                        busy = false,
                        displayName = trimmedNewName
                    )
                }
                FriendsSettingsViewModel.triggerUpdate()
            } catch (ex: Exception) {
                setError(ex = ex, critical = false)
            }
        }
    }

    private fun toggleLibraryShare(libraryId: Int) {
        _uiState.update {
            it.copy(
                busy = true
            )
        }

        viewModelScope.launch {
            try {
                val libraryFriendLink = LibraryFriendLink(
                    friendId = _friendshipId,
                    libraryId = libraryId
                )

                if (_uiState.value.myLibs.first { it.id == libraryId }.shared) {
                    friendsRepository.unShareLibrary(libraryFriendLink)
                } else {
                    friendsRepository.shareLibrary(libraryFriendLink)
                }

                val libs = mutableListOf<ShareableLibrary>()
                for (oldLib in _uiState.value.myLibs) {
                    if (oldLib.id == libraryId) {
                        libs.add(
                            ShareableLibrary(
                                id = oldLib.id,
                                name = oldLib.name,
                                isTV = oldLib.isTV,
                                shared = !oldLib.shared
                            )
                        )
                    } else {
                        libs.add(oldLib)
                    }
                }

                _uiState.update {
                    it.copy(
                        busy = false,
                        myLibs = libs
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, critical = false)
            }
        }
    }

    private fun unfriend() {
        _uiState.update {
            it.copy(busy = true)
        }
        viewModelScope.launch {
            try {
                friendsRepository.unfriend(_friendshipId)
                FriendsSettingsViewModel.triggerUpdate()
                popBackStack()
            } catch (ex: Exception) {
                setError(ex = ex, critical = false)
            }
        }
    }


}