package tv.dustypig.dustypig.api.models

import java.util.Date

data class Notification (
    val id: Int,
    val profileId: Int,
    val title: String,
    val message: String,
    val deepLink: String?,
    var seen: Boolean,
    val timestamp: Date
)
