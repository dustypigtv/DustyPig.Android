package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class PlaylistItem (
    val id: Int,
    val index: Int,
    @SerializedName("media_id") val mediaId: Int,
    @SerializedName("series_id") val seriesId: Int,
    @SerializedName("media_type") val mediaType: Int,
    val title: String,
    val description: String,
    @SerializedName("artwork_url") val artworkUrl: String,
    val played: Double?,
    val length: Double,
    @SerializedName("bif_url") val bifUrl: String,
    @SerializedName("video_url") val videoUrl: String,
    @SerializedName("srt_subtitles") val externalSubtitles: List<ExternalSubtitle>?
)