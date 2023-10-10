package tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object ProfilesSettingsNav: NavRoute<ProfilesSettingsViewModel> {
    override val route = "profileSettings"

    @Composable
    override fun viewModel(): ProfilesSettingsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: ProfilesSettingsViewModel) = ProfilesSettingsScreen(vm = viewModel)
}