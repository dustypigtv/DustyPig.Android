package tv.dustypig.dustypig.api.models

import java.util.UUID

data class BasicFriend (
    val id: Int,
    val displayName: String,
    val avatarUrl: String,

    @Transient val clientUUID: UUID = UUID.randomUUID()
)