package tv.dustypig.dustypig.ui.main_app.screens.episode_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object EpisodeDetailsNav : NavRoute<EpisodeDetailsViewModel> {

    const val KEY_ID = "KEY_ID"
    const val KEY_CAN_PLAY = "KEY_CAN_PLAY"
    const val KEY_FROM_SERIES_DETAILS = "KEY_FROM_SERIES_DETAILS-"

    override val route= "episodeDetails/{$KEY_ID}/{$KEY_CAN_PLAY}/{$KEY_FROM_SERIES_DETAILS}"

    /**
     * fromSeriesDetails is weather this screen will be navigated to from the SeriesDetails screen,
     * or from a PlaylistDetails screen. For the PlaylistDetails screen, there will be a "Go To Series" link shown
     */
    fun getRoute(id: Int, canPlay: Boolean, fromSeriesDetails: Boolean): String = route
        .replace("{$KEY_ID}", "$id")
        .replace("{$KEY_CAN_PLAY}", "$canPlay")
        .replace("{$KEY_FROM_SERIES_DETAILS}", "$fromSeriesDetails")

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_ID) { type = NavType.IntType },
        navArgument(KEY_CAN_PLAY) { type = NavType.BoolType },
        navArgument(KEY_FROM_SERIES_DETAILS) { type = NavType.BoolType }
    )


    @Composable
    override fun viewModel(): EpisodeDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: EpisodeDetailsViewModel) = EpisodeDetailsScreen(vm = viewModel)
}