package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedSeries (
    val id: Int = 0,
    val title: String = "",
    val description: String? = null,
    @SerializedName("artwork_url") val artworkUrl: String = "",
    @SerializedName("backdrop_url") val backdropUrl: String? = null,
    val cast: List<String>? = null,
    val directors: List<String>? = null,
    val producers: List<String>? = null,
    val writers: List<String>? = null,
    val owner: String? = null,
    val rated: TVRatings = TVRatings.None,
    val genres: Long = 0,
    @SerializedName("in_watchlist") val inWatchlist: Boolean = false,
    @SerializedName("can_play") val canPlay: Boolean = false,
    @SerializedName("can_manage") val canManage: Boolean = false,
    @SerializedName("title_request_permission") val titleRequestPermissions: TitleRequestPermissions = TitleRequestPermissions.Disabled,
    @SerializedName("access_request_status") val accessRequestStatus: OverrideRequestStatus = OverrideRequestStatus.NotRequested,
    val subscribed: Boolean = false,
    val episodes: List<DetailedEpisode>? = null
)
