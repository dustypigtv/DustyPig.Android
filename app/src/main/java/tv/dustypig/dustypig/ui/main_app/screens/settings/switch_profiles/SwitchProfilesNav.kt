package tv.dustypig.dustypig.ui.main_app.screens.settings.switch_profiles

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object SwitchProfilesNav: NavRoute<SwitchProfilesViewModel> {
    override val route = "switchProfiles"

    @Composable
    override fun viewModel(): SwitchProfilesViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: SwitchProfilesViewModel) = SwitchProfilesScreen(vm = viewModel)
}