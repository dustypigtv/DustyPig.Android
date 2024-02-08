package tv.dustypig.dustypig.api.models

data class DetailedTMDB (
    val tmdbId: Int,
    val mediaType: TMDBMediaTypes = TMDBMediaTypes.Movie,
    val artworkUrl: String?,
    val backdropUrl: String?,
    val title: String,
    val year: Int,
    val rated: String?,
    val description: String?,
    val genres: Long = 0,
    val cast: List<String> = listOf(),
    val directors: List<String> = listOf(),
    val producers: List<String> = listOf(),
    val writers: List<String> = listOf(),
    val available: List<BasicMedia> = listOf(),
    val requestPermission: TitleRequestPermissions = TitleRequestPermissions.Enabled,
    val requestStatus: RequestStatus = RequestStatus.NotRequested
)
