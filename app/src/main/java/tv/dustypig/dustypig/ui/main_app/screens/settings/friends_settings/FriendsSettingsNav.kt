package tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object FriendsSettingsNav : NavRoute<FriendsSettingsViewModel> {
    override val route = "friendsSettings"

    @Composable
    override fun viewModel(): FriendsSettingsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: FriendsSettingsViewModel) =
        FriendsSettingsScreen(vm = viewModel)
}