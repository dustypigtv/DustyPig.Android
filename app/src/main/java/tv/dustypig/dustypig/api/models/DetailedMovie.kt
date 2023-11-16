package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class DetailedMovie (
    val id: Int = 0,
    val title: String = "",
    val description: String? = null,
    @SerializedName("artwork_url") val artworkUrl: String = "",
    @SerializedName("artwork_size") val artworkSize: ULong = 0U,
    @SerializedName("backdrop_url") val backdropUrl: String? = null,
    @SerializedName("backdrop_size") val backdropSize: ULong = 0U,
    val cast: List<String>? = null,
    val directors: List<String>? = null,
    val producers: List<String>? = null,
    val writers: List<String>? = null,
    val owner: String? = null,
    val played: Double? = null,
    val rated: MovieRatings = MovieRatings.None,
    val genres: Long = 0,
    @SerializedName("bif_url") val bifUrl: String? = null,
    @SerializedName("bif_size") val bifSize: ULong = 0U,
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("video_size") val videoSize: ULong = 0U,
    @SerializedName("in_watchlist") val inWatchlist: Boolean = false,
    @SerializedName("can_play") val canPlay: Boolean = false,
    @SerializedName("can_manage") val canManage: Boolean = false,
    val date: Date = Date(),
    val length: Double = 0.0,
    @SerializedName("intro_start_time") val introStartTime: Double? = null,
    @SerializedName("intro_end_time") val introEndTime: Double? = null,
    @SerializedName("credit_start_time") val creditStartTime: Double? = null,
    @SerializedName("srt_subtitles") val externalSubtitles: List<ExternalSubtitle>? = null,
    @SerializedName("title_request_permission") val titleRequestPermissions: TitleRequestPermissions = TitleRequestPermissions.Disabled,
    @SerializedName("access_request_status") val accessRequestStatus: OverrideRequestStatus = OverrideRequestStatus.NotRequested,
) {
    fun displayTitle(): String{
        val year = SimpleDateFormat("yyyy", Locale.US).format(date)
        return "$title ($year)"
    }
}
