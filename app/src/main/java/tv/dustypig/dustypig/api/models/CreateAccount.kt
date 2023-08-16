package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class CreateAccount(
    val email:String,
    val password:String,
    @SerializedName("display_name") val displayName:String? = null,
    @SerializedName("avatar_url") val avatarUrl:String? = null,
    @SerializedName("fcm_token") val fcmToken:String? = null
)