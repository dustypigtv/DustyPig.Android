package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedEpisode(
    val id: Int = 0,
    @SerializedName("bif_url") val bifUrl: String? = null,
    @SerializedName("bif_size") val bifSize: ULong = 0U,
    @SerializedName("video_url") val videoUrl: String = "",
    @SerializedName("video_size") val videoSize: ULong = 0U,
    @SerializedName("srt_subtitles") val externalSubtitles: List<ExternalSubtitle>? = null,
    val played: Double? = null,
    @SerializedName("up_next") val upNext: Boolean = false,
    val title: String = "",
    val description: String? = null,
    @SerializedName("artwork_url") val artworkUrl: String = "",
    @SerializedName("artwork_size") val artworkSize: ULong = 0U,
    val length: Double = 0.0,
    @SerializedName("intro_start_time") val introStartTime: Double? = null,
    @SerializedName("intro_end_time") val introEndTime: Double? = null,
    @SerializedName("credit_start_time") val creditStartTime: Double? = null,
    @SerializedName("season_number") val seasonNumber: UShort = 0u,
    @SerializedName("episode_number") val episodeNumber: UShort= 0u,
    @SerializedName("series_id") val seriesId: Int = 0,
    @SerializedName("series_title") val seriesTitle: String? = null
) {
    fun fullDisplayTitle() = "S$seasonNumber:E$episodeNumber - $title"
    fun shortDisplayTitle() = "E$episodeNumber: $title"
}
