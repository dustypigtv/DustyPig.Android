package tv.dustypig.dustypig.ui.main_app.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.auth_manager.AuthManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings.AccountSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.download_settings.DownloadSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.FriendsSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.notification_settings.NotificationSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.playback_settings.PlaybackSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.ProfilesSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile.EditProfileNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.switch_profiles.SwitchProfilesNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.theme_settings.ThemeSettingsNav
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val settingsManager: SettingsManager,
    private val authManager: AuthManager
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        SettingsUIState(
            onNavToRoute = ::navigateToRoute,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.profileIsMainFlow.collectLatest { isMainProfile ->

                val links = ArrayList<SettingLink>()
                links.add(SettingLink( R.string.playback_settings, PlaybackSettingsNav.route))
                links.add(SettingLink( R.string.notification_settings, NotificationSettingsNav.route))
                links.add(SettingLink( R.string.download_settings, DownloadSettingsNav.route))
                links.add(SettingLink( R.string.theme, ThemeSettingsNav.route))
                links.add(SettingLink( R.string.switch_profiles, SwitchProfilesNav.route))
                links.add(SettingLink(R.string.account_settings, AccountSettingsNav.route))

                if(isMainProfile) {
                   links.add(SettingLink(R.string.manage_profiles, ProfilesSettingsNav.route))
                   links.add(SettingLink(R.string.manage_friends, FriendsSettingsNav.route))
                } else {
                    links.add(SettingLink(R.string.my_profile, EditProfileNav.getRoute(authManager.currentProfileId)))
                }


                _uiState.update {
                    it.copy(links)
                }
            }
        }
    }
}