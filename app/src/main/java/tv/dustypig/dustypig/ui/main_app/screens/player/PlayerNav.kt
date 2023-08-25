package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object PlayerNav : NavRoute<PlayerViewModel> {

    private const val KEY_ID = "KEY_ID"

    override val route= "player/{$KEY_ID}"

    /**
     * Returns the route that can be used for navigating to this page.
     */
    fun getRouteForId(id: Int): String = route.replace("{$KEY_ID}", "$id")


    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_ID) { type = NavType.IntType }
    )


    @Composable
    override fun viewModel(): PlayerViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: PlayerViewModel) = PlayerScreen(vm = viewModel)

}