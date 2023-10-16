package tv.dustypig.dustypig.ui.main_app.screens.settings.notification_settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
class NotificationSettingsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    private val settingsManager: SettingsManager
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(NotificationSettingsUIState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    allowAlerts = settingsManager.getSystemLevelAllowNotifications()
                            && settingsManager.getAllowNotifications(),
                )
            }
        }
    }

    fun hideAlertsDialog(context: Context) {
        _uiState.update {
            it.copy(
                showAlertsDialog = false
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val settingsIntent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            context.startActivity(settingsIntent)
        }
    }

    fun setAllowAlerts(value: Boolean) {

        viewModelScope.launch {

            if(value && !settingsManager.getSystemLevelAllowNotifications()) {
                _uiState.update {
                    it.copy(
                        showAlertsDialog = true
                    )
                }
            } else {
                settingsManager.setAllowNotifications(value)
                _uiState.update {
                    it.copy(
                        allowAlerts = value
                    )
                }
            }
        }
    }
}