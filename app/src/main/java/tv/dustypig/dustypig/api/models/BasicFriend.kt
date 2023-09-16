package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class BasicFriend (
    val id: Int,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("avatar_url") val avatarUrl: String
)