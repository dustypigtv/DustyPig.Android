package tv.dustypig.dustypig.ui.main_app.screens.search.tmdb_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object TMDBDetailsNav : NavRoute<TMDBDetailsViewModel> {

    const val KEY_MEDIA_ID = "KEY_MEDIA_ID"
    const val KEY_CACHE_ID = "KEY_CACHE_ID"
    const val KEY_IS_MOVIE = "KEY_IS_MOVIE"

    override val route = "tmdbDetails/{$KEY_MEDIA_ID}/{$KEY_CACHE_ID}/{$KEY_IS_MOVIE}"

    fun getRoute(
        mediaId: Int,
        cacheId: String,
        isMovie: Boolean
    ): String = route
        .replace("{$KEY_MEDIA_ID}", "$mediaId")
        .replace("{$KEY_CACHE_ID}", cacheId)
        .replace("{$KEY_IS_MOVIE}", "$isMovie")

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_MEDIA_ID) { type = NavType.IntType },
        navArgument(KEY_CACHE_ID) { type = NavType.StringType },
        navArgument(KEY_IS_MOVIE) { type = NavType.BoolType }
    )

    @Composable
    override fun viewModel(): TMDBDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: TMDBDetailsViewModel) = TMDBDetailsScreen(vm = viewModel)

}