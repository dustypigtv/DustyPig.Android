package tv.dustypig.dustypig.ui.main_app.screens.playlist_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object PlaylistDetailsNav : NavRoute<PlaylistDetailsViewModel> {

    const val KEY_MEDIA_ID = "KEY_MEDIA_ID"

    override val route = "playlistDetails/{$KEY_MEDIA_ID}"


    fun getRoute(mediaId: Int): String = route
        .replace("{$KEY_MEDIA_ID}", "$mediaId")


    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_MEDIA_ID) { type = NavType.IntType },
    )

    @Composable
    override fun viewModel(): PlaylistDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: PlaylistDetailsViewModel) =
        PlaylistDetailsScreen(vm = viewModel)
}