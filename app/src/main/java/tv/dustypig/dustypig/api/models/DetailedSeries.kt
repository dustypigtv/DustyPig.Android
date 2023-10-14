package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedSeries (
    val id: Int,
    val title: String,
    val description: String?,
    @SerializedName("artwork_url") val artworkUrl: String,
    @SerializedName("backdrop_url") val backdropUrl: String?,
    val cast: List<String>?,
    val directors: List<String>?,
    val producers: List<String>?,
    val writers: List<String>?,
    val owner: String?,
    val rated: TVRatings,
    val genres: Long,
    @SerializedName("in_watchlist") val inWatchlist: Boolean = false,
    @SerializedName("can_play") val canPlay: Boolean = false,
    @SerializedName("can_manage") val canManage: Boolean = false,
    @SerializedName("title_request_permission") val titleRequestPermissions: TitleRequestPermissions,
    @SerializedName("access_request_status") val accessRequestStatus: OverrideRequestStatus,
    val episodes: List<DetailedEpisode>?
)
