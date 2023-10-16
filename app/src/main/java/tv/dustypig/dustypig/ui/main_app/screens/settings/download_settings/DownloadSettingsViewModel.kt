package tv.dustypig.dustypig.ui.main_app.screens.settings.download_settings

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
class DownloadSettingsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    private val settingsManager: SettingsManager
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(DownloadSettingsUIState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val dom = settingsManager.getDownloadOverMobile()
            _uiState.update {
                it.copy(
                    downloadOverMobile = dom
                )
            }
        }
    }

    fun setDownloadOverMobile(value: Boolean) {
        viewModelScope.launch {
            settingsManager.setDownloadOverMobile(value)
            _uiState.update {
                it.copy(
                    downloadOverMobile = value
                )
            }
        }
    }
}




















