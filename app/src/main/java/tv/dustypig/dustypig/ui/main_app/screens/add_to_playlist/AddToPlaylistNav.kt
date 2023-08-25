package tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object AddToPlaylistNav : NavRoute<AddToPlaylistViewModel> {

    const val KEY_ID = "KEY_ID"
    const val KEY_IS_SERIES = "KEY_IS_SERIES"

    override val route= "addToPlaylist/{$KEY_ID}/{$KEY_IS_SERIES}"

    /**
     * Returns the route that can be used for navigating to this page.
     */
    fun getRouteForId(id: Int, isSeries: Boolean): String =
        route
            .replace("{$KEY_ID}", "$id")
            .replace("{$KEY_IS_SERIES}", "$isSeries")


    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_ID) { type = NavType.IntType }
    )


    @Composable
    override fun viewModel(): AddToPlaylistViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: AddToPlaylistViewModel) = AddToPlaylistScreen(vm = viewModel)
}