package tv.dustypig.dustypig.ui.main_app.screens.settings.notification_settings

import android.content.Context

data class NotificationSettingsUIState(

    //Data
    val allowAlerts: Boolean = false,
    val showAlertsDialog: Boolean = false,

    //Event
    val onPopBackStack: () -> Unit = { },
    val onSetAllowAlerts: (value: Boolean) -> Unit = { },
    val onHideAlertsDialog: (context: Context) -> Unit = { }
)
