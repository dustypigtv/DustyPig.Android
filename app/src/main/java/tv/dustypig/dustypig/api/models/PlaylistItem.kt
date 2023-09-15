package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class PlaylistItem (
    val id: Int,
    val index: Int,
    @SerializedName("media_id") val mediaId: Int,
    @SerializedName("series_id") val seriesId: Int,
    @SerializedName("media_type") val mediaType: MediaTypes,
    val title: String,
    val description: String,
    @SerializedName("artwork_url") val artworkUrl: String,
    val played: Double?,
    val length: Double,
    @SerializedName("intro_start_time") val introStartTime: Double?,
    @SerializedName("intro_end_time") val introEndTime: Double?,
    @SerializedName("credit_start_time") val creditStartTime: Double?,
    @SerializedName("bif_url") val bifUrl: String,
    @SerializedName("video_url") val videoUrl: String,
    @SerializedName("srt_subtitles") val externalSubtitles: List<ExternalSubtitle>?
)