package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class ProfileCredentials(
    val id: Int,
    val pin: Int? = null,
    @SerializedName("fcm_token") val fcmToken: String? = null
)