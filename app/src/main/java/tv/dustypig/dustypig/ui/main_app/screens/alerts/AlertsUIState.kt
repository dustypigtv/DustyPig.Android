package tv.dustypig.dustypig.ui.main_app.screens.alerts

import tv.dustypig.dustypig.api.models.Notification

data class AlertsUIState(
    val busy: Boolean = false,
    val notifications: List<Notification> = listOf()
)