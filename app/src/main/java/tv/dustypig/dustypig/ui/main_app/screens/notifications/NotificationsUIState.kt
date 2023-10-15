package tv.dustypig.dustypig.ui.main_app.screens.notifications

import tv.dustypig.dustypig.api.models.Notification

data class NotificationsUIState(
    val busy: Boolean = false,
    val notifications: List<Notification> = listOf()
)
