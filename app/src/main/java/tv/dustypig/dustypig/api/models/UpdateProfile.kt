package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class UpdateProfile(
    val id: Int,
    val name: String,
    val locked: Boolean,

    /**
     * Gson won't serialize a UShort? correctly, so make sure to double check this when setting the value
     */
    val pin: Int? = null,

    @SerializedName("clear_pin") val clearPin: Boolean = false,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("max_movie_rating") val maxMovieRating: MovieRatings,
    @SerializedName("max_tv_rating") val maxTVRating: TVRatings,
    @SerializedName("title_request_permissions") val titleRequestPermissions: TitleRequestPermissions
)