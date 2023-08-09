package tv.dustypig.dustypig.api.models

data class CreateAccount(
    val email:String,
    val password:String,
    val display_name:String? = null,
    val avatar_url:String? = null,
    val fcm_token:String? = null
)