package tv.dustypig.dustypig.ui.main_app.screens.show_more

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute


object ShowMoreNav : NavRoute<ShowMoreViewModel> {

    override val route = "showMore"

    @Composable
    override fun viewModel(): ShowMoreViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: ShowMoreViewModel) = ShowMoreScreen(viewModel)


}