package tv.dustypig.dustypig.ui.main_app.screens.series_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object SeriesDetailsNav : NavRoute<SeriesDetailsViewModel> {

    const val KEY_ID = "KEY_ID"

    override val route = "seriesDetails/{$KEY_ID}"


    fun getRouteForId(id: Int): String = route.replace("{$KEY_ID}", "$id")

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_ID) { type = NavType.IntType }
    )

    @Composable
    override fun viewModel(): SeriesDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: SeriesDetailsViewModel) = SeriesDetailsScreen(vm = viewModel)

}