package tv.dustypig.dustypig.api.models

data class DetailedSeries (
    val id: Int = 0,
    val title: String = "",
    val description: String? = null,
    val artworkUrl: String = "",
    val artworkSize: ULong = 0U,
    val backdropUrl: String? = null,
    val backdropSize: ULong = 0U,
    val cast: List<String>? = null,
    val directors: List<String>? = null,
    val producers: List<String>? = null,
    val writers: List<String>? = null,
    val owner: String? = null,
    val rated: TVRatings = TVRatings.None,
    val genres: Long = 0,
    val inWatchlist: Boolean = false,
    val canPlay: Boolean = false,
    val canManage: Boolean = false,
    val titleRequestPermission: TitleRequestPermissions = TitleRequestPermissions.Disabled,
    val accessRequestedStatus: OverrideRequestStatus = OverrideRequestStatus.NotRequested,
    val subscribed: Boolean = false,
    val episodes: List<DetailedEpisode>? = null
)
