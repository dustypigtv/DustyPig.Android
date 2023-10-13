package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedEpisode(
    val id: Int,
    @SerializedName("bif_url") val bifUrl: String?,
    @SerializedName("video_url") val videoUrl: String,
    @SerializedName("srt_subtitles") val externalSubtitles: List<ExternalSubtitle>?,
    val played: Double?,
    @SerializedName("up_next") val upNext: Boolean,
    val title: String,
    val description: String?,
    @SerializedName("artwork_url") val artworkUrl: String,
    val length: Double,
    @SerializedName("intro_start_time") val introStartTime: Double?,
    @SerializedName("intro_end_time") val introEndTime: Double?,
    @SerializedName("credit_start_time") val creditStartTime: Double?,
    @SerializedName("season_number") val seasonNumber: UShort,
    @SerializedName("episode_number") val episodeNumber: UShort,
    @SerializedName("series_id") val seriesId: Int,
    @SerializedName("series_title") val seriesTitle: String?
) {
    fun fullDisplayTitle() = "S$seasonNumber:E$episodeNumber - $title"
    fun shortDisplayTitle() = "E$episodeNumber: $title"
}
