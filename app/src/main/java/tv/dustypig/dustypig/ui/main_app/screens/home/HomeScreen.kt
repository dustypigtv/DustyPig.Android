package tv.dustypig.dustypig.ui.main_app.screens.home

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute


object HomeScreenRoute : NavRoute<HomeScreenViewModel> {

    override val route = "home"

    @Composable
    override fun viewModel(): HomeScreenViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: HomeScreenViewModel) = HomeScreen(viewModel)
}

@Composable
fun HomeScreen(vm: HomeScreenViewModel) {


}


