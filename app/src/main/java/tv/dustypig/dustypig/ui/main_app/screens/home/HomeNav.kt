package tv.dustypig.dustypig.ui.main_app.screens.home

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute


object HomeNav : NavRoute<HomeViewModel> {

    override val route = "home"

    @Composable
    override fun viewModel(): HomeViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: HomeViewModel) = HomeScreen(viewModel)
}