package tv.dustypig.dustypig.api.models

data class PasswordCredentials(
    val email: String,
    val password: String,
    val fcmToken: String? = null
)