package tv.dustypig.dustypig.ui.main_app.screens.tmdb_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object TMDBDetailsNav : NavRoute<TMDBDetailsViewModel> {

    const val KEY_ID = "KEY_ID"
    const val KEY_IS_MOVIE = "KEY_IS_MOVIE"

    override val route = "tmdbDetails/{$KEY_ID}/{$KEY_IS_MOVIE}"

    fun getRouteForId(id: Int, isMovie: Boolean): String = route
        .replace("{$KEY_ID}", "$id")
        .replace("{$KEY_IS_MOVIE}", "$isMovie")

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_ID) { type = NavType.IntType },
        navArgument(KEY_IS_MOVIE) { type = NavType.BoolType }
    )

    @Composable
    override fun viewModel(): TMDBDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: TMDBDetailsViewModel) = TMDBDetailsScreen(vm = viewModel)

}