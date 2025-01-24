package tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object EditProfileNav : NavRoute<EditProfileViewModel> {

    override val route = "editProfile"

    @Composable
    override fun viewModel(): EditProfileViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: EditProfileViewModel) = EditProfileScreen(vm = viewModel)
}