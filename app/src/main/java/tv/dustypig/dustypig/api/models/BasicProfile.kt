package tv.dustypig.dustypig.api.models

data class BasicProfile(
    val id: Int = 0,
    val name: String = "",
    val avatar_url: String? = null,
    val is_main: Boolean = false,
    val has_pin: Boolean = false
)
