package tv.dustypig.dustypig.api.models

data class UpdateFriend(
    val id: Int,
    val accepted: Boolean,
    val displayName: String
)
