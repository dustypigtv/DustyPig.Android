package tv.dustypig.dustypig.api.models

import java.util.UUID

data class BasicProfile(
    val id: Int = 0,
    val name: String = "",
    val initials: String,
    val avatarUrl: String? = null,
    val isMain: Boolean = false,
    val hasPin: Boolean = false,

    @Transient val clientUUID: UUID = UUID.randomUUID()
)
