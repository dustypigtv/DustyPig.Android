package tv.dustypig.dustypig.api.models

data class BasicPerson(
    val tmdbId: Int,
    val name: String,
    val initials: String,
    val avatarUrl: String? = null,
    val order: Int,
    val role: CreditRoles
)
