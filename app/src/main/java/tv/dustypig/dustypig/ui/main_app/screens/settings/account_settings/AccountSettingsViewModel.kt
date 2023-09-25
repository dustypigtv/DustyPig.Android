package tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import tv.dustypig.dustypig.api.repositories.AccountRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val authManager: AuthManager,
    private val accountRepository: AccountRepository
): ViewModel(), RouteNavigator by routeNavigator {

}