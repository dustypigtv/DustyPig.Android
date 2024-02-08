package tv.dustypig.dustypig.api.models

data class CreateProfile(
    val name: String,

    /**
     * Gson won't serialize a UShort? correctly, so make sure to double check this when setting the value
     */
    val pin: Int?,

    val locked: Boolean,
    val avatarUrl: String?,
    val maxMovieRating: MovieRatings,
    val maxTVRating: TVRatings,
    val titleRequestPermissions: TitleRequestPermissions
)