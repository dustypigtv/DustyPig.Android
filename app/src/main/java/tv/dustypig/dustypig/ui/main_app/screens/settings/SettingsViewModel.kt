package tv.dustypig.dustypig.ui.main_app.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile.EditProfileNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile.EditProfileViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val settingsManager: SettingsManager,
    private val authManager: AuthManager
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(SettingsUIState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.profileIsMainFlow.collectLatest { isMainProfile ->
                _uiState.update {
                    it.copy(
                        isMainProfile = isMainProfile
                    )
                }
            }
        }
    }

    fun navToMyProfile() {
        EditProfileViewModel.preloadAvatar = ""
        EditProfileViewModel.selectedProfileId = authManager.currentProfileId
        navigateToRoute(EditProfileNav.route)
    }
}