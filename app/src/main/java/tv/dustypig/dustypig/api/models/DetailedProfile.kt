package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedProfile(
    val id: Int,
    val name: String,
    val pin: UShort?,
    val locked: Boolean,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("allowed_ratings") val allowedRatings: Int,
    @SerializedName("title_request_permissions") val titleRequestPermissions: TitleRequestPermissions
)

