package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
    val rated: MovieRatings,
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
) {
    fun displayTitle(): String{
        val year = SimpleDateFormat("yyyy", Locale.US).format(date)
        return "$title ($year)"
    }
}



