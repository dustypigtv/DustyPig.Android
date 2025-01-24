package tv.dustypig.dustypig.ui.main_app.screens.settings.download_settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object DownloadSettingsNav : NavRoute<DownloadSettingsViewModel> {
    override val route = "downloadSettings"

    @Composable
    override fun viewModel(): DownloadSettingsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: DownloadSettingsViewModel) =
        DownloadSettingsScreen(vm = viewModel)
}