package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute
import java.util.UUID

object MovieDetailsNav : NavRoute<MovieDetailsViewModel> {

    const val KEY_MEDIA_ID = "KEY_MEDIA_ID"
    const val KEY_CACHE_ID = "KEY_CACHE_ID"

    override val route = "movieDetails/{$KEY_MEDIA_ID}/{$KEY_CACHE_ID}"

    fun getRoute(
        mediaId: Int,
        cacheId: String = UUID.randomUUID().toString()
    ): String = route
        .replace("{$KEY_MEDIA_ID}", "$mediaId")
        .replace("{$KEY_CACHE_ID}", cacheId)

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_MEDIA_ID) { type = NavType.IntType },
        navArgument(KEY_CACHE_ID) { type = NavType.StringType }
    )

    @Composable
    override fun viewModel(): MovieDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: MovieDetailsViewModel) = MovieDetailsScreen(vm = viewModel)

}