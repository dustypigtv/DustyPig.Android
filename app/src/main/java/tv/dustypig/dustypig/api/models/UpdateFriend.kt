package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class UpdateFriend(
    val id: Int,
    val accepted: Boolean,
    @SerializedName("display_name") val displayName: String
)
