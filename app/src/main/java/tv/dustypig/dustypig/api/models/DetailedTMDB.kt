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
    val credits: List<BasicPerson>? = null,
    val available: List<BasicMedia>? = null,
    val requestPermission: TitleRequestPermissions = TitleRequestPermissions.Enabled,
    val requestStatus: RequestStatus = RequestStatus.NotRequested
)
