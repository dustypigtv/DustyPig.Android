package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Notification (
    val id: Int,
    val profileId: Int,
    val title: String,
    val message: String,
    @SerializedName("deep_link") val deepLink: String?,
    var seen: Boolean,
    val timestamp: Date
)
