package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedProfile(
    val id: Int,
    val name: String,
    val locked: Boolean,
    @SerializedName("has_pin") val hasPin: Boolean,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("allowed_ratings") val allowedRatings: Int,
    @SerializedName("title_request_permissions") val titleRequestPermissions: TitleRequestPermissions
)

