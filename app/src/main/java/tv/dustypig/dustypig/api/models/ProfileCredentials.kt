package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class ProfileCredentials(
    val id: Int,

    /**
     * This is actually a UShort?, but Gson throws an error.  So double check that a value is a UShort? before setting it!
     */
    val pin: Int? = null,

    @SerializedName("fcm_token") val fcmToken: String? = null
)