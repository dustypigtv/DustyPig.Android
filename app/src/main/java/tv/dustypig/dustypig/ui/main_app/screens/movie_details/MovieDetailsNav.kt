package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object MovieDetailsNav : NavRoute<MovieDetailsViewModel> {

    override val route = "movieDetails"


    @Composable
    override fun viewModel(): MovieDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: MovieDetailsViewModel) = MovieDetailsScreen(vm = viewModel)

}