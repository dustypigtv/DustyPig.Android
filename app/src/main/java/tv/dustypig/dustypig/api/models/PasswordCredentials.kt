package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class PasswordCredentials(
    val email: String,
    val password: String,
    @SerializedName("fcm_token") val fcmToken: String? = null
)