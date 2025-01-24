package tv.dustypig.dustypig.ui.main_app.screens.settings.theme_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.global_managers.settings_manager.Themes
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val settingsManager: SettingsManager
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        ThemeSettingsUIState(
            onPopBackStack = ::popBackStack,
            onSetTheme = ::setTheme
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.themeFlow.collectLatest { theme ->
                _uiState.update {
                    it.copy(
                        currentTheme = theme
                    )
                }
            }
        }
    }

    private fun setTheme(theme: Themes) {
        viewModelScope.launch {
            settingsManager.setTheme(theme)
        }
    }
}