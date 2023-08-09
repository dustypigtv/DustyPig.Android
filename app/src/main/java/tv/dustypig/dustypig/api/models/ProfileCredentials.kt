package tv.dustypig.dustypig.api.models

data class ProfileCredentials(
    val id: Int,
    val pin: Int?,
    val fcm_token: String?
)