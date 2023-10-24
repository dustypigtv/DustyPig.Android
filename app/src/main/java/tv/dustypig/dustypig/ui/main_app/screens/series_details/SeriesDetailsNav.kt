package tv.dustypig.dustypig.ui.main_app.screens.series_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object SeriesDetailsNav : NavRoute<SeriesDetailsViewModel> {

    const val KEY_MEDIA_ID = "KEY_MEDIA_ID"
    const val KEY_BASIC_CACHE_ID = "KEY_BASIC_CACHE_ID"

    override val route = "seriesDetails/{$KEY_MEDIA_ID}/{$KEY_BASIC_CACHE_ID}"


    fun getRoute(
        mediaId: Int,
        basicCacheId: String
    ): String = route
        .replace("{$KEY_MEDIA_ID}", "$mediaId")
        .replace("{$KEY_BASIC_CACHE_ID}", basicCacheId)

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_MEDIA_ID) { type = NavType.IntType },
        navArgument(KEY_BASIC_CACHE_ID) { type = NavType.StringType }
    )

    @Composable
    override fun viewModel(): SeriesDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: SeriesDetailsViewModel) = SeriesDetailsScreen(vm = viewModel)

}