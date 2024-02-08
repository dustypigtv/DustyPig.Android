package tv.dustypig.dustypig.api.models

data class CreateAccount(
    val email:String,
    val password:String,
    val displayName:String? = null,
    val avatarUrl:String? = null,
    val fcmToken:String? = null
)