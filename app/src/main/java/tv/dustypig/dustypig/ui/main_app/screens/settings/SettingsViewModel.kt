package tv.dustypig.dustypig.ui.main_app.screens.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings.AccountSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.theme_settings.ThemeSettingsNav
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val authManager: AuthManager
): ViewModel(), RouteNavigator by routeNavigator {

    fun navToTheme() = navigateToRoute(ThemeSettingsNav.route)

    fun navToAccountSettings() = navigateToRoute(AccountSettingsNav.route)


}