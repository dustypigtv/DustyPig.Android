package tv.dustypig.dustypig.api.models

data class DetailedProfile(
    val id: Int,
    val name: String,
    val locked: Boolean,
    val hasPin: Boolean,
    val avatarUrl: String,
    val maxMovieRating: MovieRatings,
    val maxTVRating: TVRatings,
    val titleRequestPermissions: TitleRequestPermissions,
    val availableLibraries: List<BasicLibrary>
)

