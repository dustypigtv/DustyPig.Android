package tv.dustypig.dustypig.ui.main_app.screens.settings.playback_settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object PlaybackSettingsNav: NavRoute<PlaybackSettingsViewModel> {
    override val route = "playbackSettings"

    @Composable
    override fun viewModel(): PlaybackSettingsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: PlaybackSettingsViewModel) = PlaybackSettingsScreen(vm = viewModel)
}