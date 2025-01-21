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
import tv.dustypig.dustypig.global_managers.AlertsManager
import tv.dustypig.dustypig.global_managers.FCMManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    private val settingsManager: SettingsManager
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        NotificationSettingsUIState(
            onPopBackStack = ::popBackStack,
            onSetAllowAlerts = ::setAllowAlerts,
            onHideAlertsDialog = ::hideAlertsDialog
        )
    )
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

    private fun hideAlertsDialog(context: Context) {
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

    private fun setAllowAlerts(value: Boolean) {

        viewModelScope.launch {

            if(value && !settingsManager.getSystemLevelAllowNotifications()) {
                _uiState.update {
                    it.copy(
                        showAlertsDialog = true
                    )
                }
            } else {

                //Update settings FIRST
                settingsManager.setAllowNotifications(value)

                if(value) {
                    //There should be a fcm token, so just tell dusty pig to
                    //associate it with this profile
                    AlertsManager.triggerUpdateFCMToken()
                } else {
                    //Since the device can receive messages in background mode,
                    //and it does not use the FCMManager to push alerts, we
                    //have to stop messages at the library level. Deleting the token
                    //will make it no longer work to receive messages from FCM.
                    //Calling FCMManager.resetToken generates a new token, and IF
                    //alerts are allowed it will call the onNewToken function, which will
                    //in turn call AlertsManager.updateFCMToken automatically in the background.
                    //That call will delete the now dead token from dusty pig's server
                    FCMManager.resetToken()
                }

                _uiState.update {
                    it.copy(
                        allowAlerts = value
                    )
                }
            }
        }
    }
}