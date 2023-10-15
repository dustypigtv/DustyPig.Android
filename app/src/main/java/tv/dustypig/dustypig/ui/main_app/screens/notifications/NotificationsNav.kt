package tv.dustypig.dustypig.ui.main_app.screens.notifications

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object NotificationsNav: NavRoute<NotificationsViewModel> {
    override val route = "notifications"

    @Composable
    override fun viewModel(): NotificationsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: NotificationsViewModel) = NotificationsScreen(vm = viewModel)

}