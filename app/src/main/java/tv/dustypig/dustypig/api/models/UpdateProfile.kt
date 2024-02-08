package tv.dustypig.dustypig.api.models

data class UpdateProfile(
    val id: Int,
    val name: String,
    val locked: Boolean,

    /**
     * Gson won't serialize a UShort? correctly, so make sure to double check this when setting the value
     */
    val pin: Int? = null,

    val clearPin: Boolean = false,
    val avatarUrl: String?,
    val maxMovieRating: MovieRatings,
    val maxTVRating: TVRatings,
    val titleRequestPermissions: TitleRequestPermissions
)