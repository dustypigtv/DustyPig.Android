package tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.repositories.FriendsRepository
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.friend_details_settings.FriendDetailsSettingsNav
import javax.inject.Inject

@HiltViewModel
class FriendsSettingsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    private val friendsRepository: FriendsRepository
): ViewModel(), RouteNavigator by routeNavigator  {

    private val _uiState = MutableStateFlow(FriendsSettingsUIState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try{
                val friends = friendsRepository.list()
                _uiState.update {
                    it.copy(
                        busy = false,
                        friends = friends
                    )
                }
            } catch (ex: Exception) {
                ex.logToCrashlytics()
                _uiState.update {
                    it.copy(
                        busy = false,
                        showError = true,
                        errorMessage = ex.localizedMessage
                    )
                }
            }
        }
    }

    fun hideDialog() = _uiState.update {
        it.copy(
            showError = false,
            showInviteSuccessDialog = false
        )
    }

    fun addFriend(email: String) {
        _uiState.update {
            it.copy(busy = false)
        }
        viewModelScope.launch {
            try{
                friendsRepository.invite(email)
                _uiState.update {
                    it.copy(
                        busy = false,
                        showInviteSuccessDialog =  true
                    )
                }
            } catch (ex: Exception) {
                ex.logToCrashlytics()
                _uiState.update {
                    it.copy(
                        busy = false,
                        showError = true,
                        errorMessage = ex.localizedMessage
                    )
                }
            }
        }
    }

    fun navToFriendDetails(id: Int) {
        navigateToRoute(FriendDetailsSettingsNav.getRouteForId(id))
    }


}

























