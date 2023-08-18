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
    val rated: Ratings,
    val genres: Long,
    @SerializedName("in_watchlist") val inWatchlist: Boolean = false,
    @SerializedName("can_play") val canPlay: Boolean = false,
    @SerializedName("can_manage") val canManage: Boolean = false,
    @SerializedName("access_request_status") val accessRequestStatus: OverrideRequestStatus,
    val episodes: List<DetailedEpisode>?

//    @SerializedName("tmdb_id") val tmdbId: Int?,
//    @SerializedName("library_id") val libraryId: Int?,
//    @SerializedName("extra_search_terms") val extraSearchTerms: List<String>?
)
