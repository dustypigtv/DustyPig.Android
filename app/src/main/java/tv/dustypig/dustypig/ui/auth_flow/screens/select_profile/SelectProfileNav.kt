package tv.dustypig.dustypig.ui.auth_flow.screens.select_profile

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute


object SelectProfileNav : NavRoute<SelectProfileViewModel> {

    override val route = "selectProfile"

    @Composable
    override fun viewModel(): SelectProfileViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: SelectProfileViewModel) = SelectProfileScreen(viewModel)
}
