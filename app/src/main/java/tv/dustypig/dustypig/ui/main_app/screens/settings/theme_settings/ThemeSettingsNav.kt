package tv.dustypig.dustypig.ui.main_app.screens.settings.theme_settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute


object ThemeSettingsNav : NavRoute<ThemeSettingsViewModel> {
    override val route = "settingsTheme"

    @Composable
    override fun viewModel(): ThemeSettingsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: ThemeSettingsViewModel) = SettingsThemeScreen(vm = viewModel)
}