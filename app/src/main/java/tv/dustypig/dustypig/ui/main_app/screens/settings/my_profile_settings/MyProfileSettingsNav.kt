package tv.dustypig.dustypig.ui.main_app.screens.settings.my_profile_settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object MyProfileSettingsNav: NavRoute<MyProfileSettingsViewModel> {

    override val route = "myProfileSettings"

    @Composable
    override fun viewModel(): MyProfileSettingsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: MyProfileSettingsViewModel) = MyProfileSettingsScreen(vm = viewModel)
}