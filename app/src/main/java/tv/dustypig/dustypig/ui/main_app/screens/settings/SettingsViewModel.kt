package tv.dustypig.dustypig.ui.main_app.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings.AccountSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.FriendsSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.my_profile_settings.MyProfileSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.ProfilesSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.theme_settings.ThemeSettingsNav
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val settingsManager: SettingsManager
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

    fun navToTheme() = navigateToRoute(ThemeSettingsNav.route)


    fun navToAccountSettings() = navigateToRoute(AccountSettingsNav.route)

    fun navToMyProfile() = navigateToRoute(MyProfileSettingsNav.route)

    fun navToFriendsSettings() = navigateToRoute(FriendsSettingsNav.route)

    fun navToAllProfilesSettings() = navigateToRoute(ProfilesSettingsNav.route)

}