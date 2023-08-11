package tv.dustypig.dustypig.ui.main_app.screens.downloads

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute


object DownloadsScreenRoute : NavRoute<DownloadsViewModel> {

    override val route = "downloads"

    @Composable
    override fun viewModel(): DownloadsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: DownloadsViewModel) = DownloadsScreen(viewModel)
}