package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class UpdateProfile(
    val id: Int,
    val name: String,
    val locked: Boolean,
    val pin: UShort? = null,
    @SerializedName("clear_pin") val clearPin: Boolean = false,
    @SerializedName("avatar_ur;") val avatarUrl: String?,
    @SerializedName("allowed_ratings") val allowedRatings: Int,
    @SerializedName("title_request_permissions") val titleRequestPermissions: TitleRequestPermissions
)