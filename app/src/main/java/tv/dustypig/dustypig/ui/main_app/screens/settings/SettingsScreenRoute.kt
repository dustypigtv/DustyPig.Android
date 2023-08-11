package tv.dustypig.dustypig.ui.main_app.screens.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute


object SettingsScreenRoute : NavRoute<SettingsViewModel> {

    override val route = "settings"

    @Composable
    override fun viewModel(): SettingsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: SettingsViewModel) = SettingsScreen(viewModel)
}