package tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.friend_details_settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object FriendDetailsSettingsNav: NavRoute<FriendDetailsSettingsViewModel> {

    const val KEY_ID = "KEY_ID"

    override val route = "friendDetails/{$KEY_ID}"

    fun getRouteForId(id: Int): String = route.replace("{$KEY_ID}", "$id")

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_ID) { type = NavType.IntType }
    )

    @Composable
    override fun viewModel(): FriendDetailsSettingsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: FriendDetailsSettingsViewModel) = FriendDetailsSettingsScreen(vm = viewModel)
}