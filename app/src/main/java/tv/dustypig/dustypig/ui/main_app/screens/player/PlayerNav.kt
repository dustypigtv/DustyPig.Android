package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object PlayerNav : NavRoute<PlayerViewModel> {

    const val MEDIA_TYPE_MOVIE = 1
    const val MEDIA_TYPE_SERIES = 2
    const val MEDIA_TYPE_PLAYLIST = 3


    const val KEY_MEDIA_ID = "KEY_MEDIA_ID"
    const val KEY_MEDIA_TYPE = "KEY_MEDIA_TYPE"
    const val KEY_UPNEXT_ID = "KEY_UPNEXT_ID"

    override val route= "player/{$KEY_MEDIA_ID}/{$KEY_MEDIA_TYPE}/{$KEY_UPNEXT_ID}"

    fun getRoute(mediaId: Int, sourceType: Int, upNextId: Int) = route
        .replace("{$KEY_MEDIA_ID}", "$mediaId")
        .replace("{$KEY_MEDIA_TYPE}", "$sourceType")
        .replace("{$KEY_UPNEXT_ID}", "$upNextId")


    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_MEDIA_ID) { type = NavType.IntType },
        navArgument(KEY_MEDIA_TYPE) { type = NavType.IntType },
        navArgument(KEY_UPNEXT_ID) { type = NavType.IntType }
    )


    @Composable
    override fun viewModel(): PlayerViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: PlayerViewModel) = PlayerScreen(vm = viewModel)

}