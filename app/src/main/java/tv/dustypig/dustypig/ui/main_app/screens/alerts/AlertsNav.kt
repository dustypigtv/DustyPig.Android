package tv.dustypig.dustypig.ui.main_app.screens.alerts

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object AlertsNav: NavRoute<AlertsViewModel> {
    override val route = "notifications"

    @Composable
    override fun viewModel(): AlertsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: AlertsViewModel) = AlertsScreen(vm = viewModel)

}