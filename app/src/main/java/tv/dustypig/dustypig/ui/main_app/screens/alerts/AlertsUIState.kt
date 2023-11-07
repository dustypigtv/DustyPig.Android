package tv.dustypig.dustypig.ui.main_app.screens.alerts

import tv.dustypig.dustypig.api.models.Notification

data class AlertsUIState(

    //Data
    val busy: Boolean = false,
    val notifications: List<Notification> = listOf(),

    //Events
    val onItemClicked: (id: Int) -> Unit = { },
    val onDeleteItem: (id: Int) -> Unit = { }
)
