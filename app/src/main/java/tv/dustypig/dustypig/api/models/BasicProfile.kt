package tv.dustypig.dustypig.api.models

data class BasicProfile(
    val id:Int,
    val name:String,
    val avatar_url:String?,
    val is_main:Boolean,
    val has_pin:Boolean
)
