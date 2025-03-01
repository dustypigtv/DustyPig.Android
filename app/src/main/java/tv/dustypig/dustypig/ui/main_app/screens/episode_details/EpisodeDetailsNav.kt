package tv.dustypig.dustypig.ui.main_app.screens.episode_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object EpisodeDetailsNav : NavRoute<EpisodeDetailsViewModel> {

    //If KEY_FROM_SERIES_DETAILS is true then KEY_PARENT_ID = series id
    //else KEY_PARENT_ID = playlist id

    const val KEY_MEDIA_ID = "KEY_MEDIA_ID"
    const val KEY_CAN_PLAY = "KEY_CAN_PLAY"
    const val KEY_PARENT_ID = "KEY_PARENT_ID"
    const val KEY_PLAYLIST_UPNEXT_INDEX_ID = "KEY_PLAYLIST_UPNEXT_INDEX_ID"

    const val SOURCE_SERIES_DETAILS = 0
    const val SOURCE_PLAYLIST_DETAILS = 1
    const val SOURCE_NOTIFICATION = 2

    const val KEY_SOURCE = "KEY_FROM_SERIES_DETAILS"


    override val route =
        "episodeDetails/{$KEY_PARENT_ID}/{$KEY_MEDIA_ID}/{$KEY_CAN_PLAY}/{$KEY_SOURCE}/{$KEY_PLAYLIST_UPNEXT_INDEX_ID}"

    /**
     * fromSeriesDetails is weather this screen will be navigated to from the SeriesDetails screen,
     * or from a PlaylistDetails screen. For the PlaylistDetails screen, there will be a "Go To Series" link shown
     */
    fun getRoute(
        parentId: Int,
        mediaId: Int,
        canPlay: Boolean,
        source: Int,
        playlistUpNextIndex: Int
    ): String = route
        .replace("{$KEY_PARENT_ID}", "$parentId")
        .replace("{$KEY_MEDIA_ID}", "$mediaId")
        .replace("{$KEY_CAN_PLAY}", "$canPlay")
        .replace("{$KEY_SOURCE}", "$source")
        .replace("{$KEY_PLAYLIST_UPNEXT_INDEX_ID}", "$playlistUpNextIndex")

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_PARENT_ID) { type = NavType.IntType },
        navArgument(KEY_MEDIA_ID) { type = NavType.IntType },
        navArgument(KEY_CAN_PLAY) { type = NavType.BoolType },
        navArgument(KEY_SOURCE) { type = NavType.IntType },
        navArgument(KEY_PLAYLIST_UPNEXT_INDEX_ID) { type = NavType.IntType }
    )


    @Composable
    override fun viewModel(): EpisodeDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: EpisodeDetailsViewModel) = EpisodeDetailsScreen(vm = viewModel)
}