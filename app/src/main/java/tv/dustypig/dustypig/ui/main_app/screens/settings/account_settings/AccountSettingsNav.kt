package tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object AccountSettingsNav: NavRoute<AccountSettingsViewModel> {
    override val route = "accountSettings"

    @Composable
    override fun viewModel(): AccountSettingsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: AccountSettingsViewModel) = AccountSettingsScreen(vm = viewModel)
}
