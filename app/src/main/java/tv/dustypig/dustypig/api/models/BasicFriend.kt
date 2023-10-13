package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class BasicFriend (
    val id: Int,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("avatar_url") val avatarUrl: String,

    @Transient val clientUUID: UUID = UUID.randomUUID()
)