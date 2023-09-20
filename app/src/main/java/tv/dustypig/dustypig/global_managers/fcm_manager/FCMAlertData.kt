package tv.dustypig.dustypig.global_managers.fcm_manager

data class FCMAlertData (
    val id: Int,
    val title: String,
    val message: String,
    val deepLink: String?
)
