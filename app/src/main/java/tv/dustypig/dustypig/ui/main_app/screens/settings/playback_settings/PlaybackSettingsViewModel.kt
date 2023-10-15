package tv.dustypig.dustypig.ui.main_app.screens.settings.playback_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject


@HiltViewModel
class PlaybackSettingsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    private val settingsManager: SettingsManager
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(PlaybackSettingsUIState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    autoSkipIntros = settingsManager.getSkipIntros(),
                    autoSkipCredits = settingsManager.getSkipCredits()
                )
            }
        }
    }

    fun setAutoSkipIntros(value: Boolean) {
        viewModelScope.launch {
            settingsManager.setSkipIntros(value)
            _uiState.update {
                it.copy(
                    autoSkipIntros = value
                )
            }
        }
    }

    fun setAutoSkipCredits(value: Boolean) {
        viewModelScope.launch {
            settingsManager.setSkipCredits(value)
            _uiState.update {
                it.copy(
                    autoSkipCredits = value
                )
            }
        }
    }

}