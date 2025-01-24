package tv.dustypig.dustypig.ui.main_app.screens.settings.notification_settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object NotificationSettingsNav : NavRoute<NotificationSettingsViewModel> {
    override val route = "notificationSettings"

    @Composable
    override fun viewModel(): NotificationSettingsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: NotificationSettingsViewModel) =
        NotificationSettingsScreen(vm = viewModel)
}