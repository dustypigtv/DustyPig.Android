package tv.dustypig.dustypig.ui.main_app.screens.show_more

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute


object ShowMoreScreenRoute : NavRoute<ShowMoreViewModel> {

    const val KEY_LIST_ID = "KEY_LIST_ID"
    const val KEY_LIST_TITLE = "KEY_LIST_TITLE"

    override val route = "showMore/{$KEY_LIST_ID}/{$KEY_LIST_TITLE}"

    /**
     * Returns the route that can be used for navigating to this page.
     */
    fun getRouteForListId(listId: Long, title: String): String = route.replace("{$KEY_LIST_ID}", "$listId").replace("{$KEY_LIST_TITLE}", title)


    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_LIST_ID) { type = NavType.LongType },
        navArgument(KEY_LIST_TITLE) { type = NavType.StringType }
    )

    @Composable
    override fun viewModel(): ShowMoreViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: ShowMoreViewModel) = ShowMoreScreen(viewModel)
}