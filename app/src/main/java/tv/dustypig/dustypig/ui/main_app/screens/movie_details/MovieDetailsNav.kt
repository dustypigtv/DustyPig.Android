package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object MovieDetailsNav : NavRoute<MovieDetailsViewModel> {

//    const val KEY_ID = "KEY_ID"

//    override val route = "movieDetails/{$KEY_ID}"

    override val route = "movieDetails"

//    /**
//     * Returns the route that can be used for navigating to this page.
//     */
//    fun getRouteForId(id: Int): String = route.replace("{$KEY_ID}", "$id")
//
//
//    override fun getArguments(): List<NamedNavArgument> = listOf(
//        navArgument(KEY_ID) { type = NavType.IntType }
//    )

    @Composable
    override fun viewModel(): MovieDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: MovieDetailsViewModel) = MovieDetailsScreen(vm = viewModel)

}