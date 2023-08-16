package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName
import java.util.Date


data class DetailedMovie (
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
    val played: Double?,
    val rated: Ratings,
    val genres: Long,
    @SerializedName("bif_url") val bifUrl: String?,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("in_watchlist") val inWatchlist: Boolean = false,
    @SerializedName("can_play") val canPlay: Boolean = false,
    @SerializedName("can_manage") val canManage: Boolean = false,
    val date: Date,
    val length: Double,
    @SerializedName("intro_start_time") val introStartTime: Double?,
    @SerializedName("intro_end_time") val introEndTime: Double?,
    @SerializedName("credit_start_time") val creditStartTime: Double?,
    @SerializedName("srt_subtitles") val externalSubtitles: List<ExternalSubtitle>?,
    @SerializedName("access_request_status") val accessRequestStatus: OverrideRequestStatus,

//    @SerializedName("tmdb_id") val tmdbId: Int?,
//    @SerializedName("library_id") val libraryId: Int?,
//    @SerializedName("extra_search_terms") val extraSearchTerms: List<String>?

)



