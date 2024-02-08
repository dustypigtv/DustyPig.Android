package tv.dustypig.dustypig.api.models

data class BasicTMDB (
    val tmdbId: Int,
    val mediaType: TMDBMediaTypes,
    val artworkUrl: String?,
    val backdropUrl: String?,
    val title: String
)