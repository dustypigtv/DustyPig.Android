package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class BasicProfile(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("is_main") val isMain: Boolean = false,
    @SerializedName("has_pin") val hasPin: Boolean = false,

    @Transient val clientUUID: UUID = UUID.randomUUID()
)
