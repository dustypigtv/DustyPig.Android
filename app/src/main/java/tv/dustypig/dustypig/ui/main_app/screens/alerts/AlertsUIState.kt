package tv.dustypig.dustypig.ui.main_app.screens.alerts

import tv.dustypig.dustypig.api.models.Notification

data class AlertsUIState(

    //Data
    val busy: Boolean = false,
    val loaded: Boolean = false,
    val notifications: List<Notification> = listOf(),
    val hasUnread: Boolean = false,

    //Events
    val onItemClicked: (id: Int) -> Unit = { },
    val onDeleteItem: (id: Int) -> Unit = { },
    val onMarkAllRead: () -> Unit = { },
    val onDeleteAll: () -> Unit = { }
)
