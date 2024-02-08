package tv.dustypig.dustypig.api.models

data class TitleRequest(
    val tmdbId: Int,
    val friendId: Int? = null,
    val mediaType: TMDBMediaTypes
)
