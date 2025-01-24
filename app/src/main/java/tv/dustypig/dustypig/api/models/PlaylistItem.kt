package tv.dustypig.dustypig.api.models

data class PlaylistItem(
    val id: Int,
    val index: Int,
    val mediaId: Int,
    val seriesId: Int,
    val mediaType: MediaTypes,
    val title: String,
    val description: String,
    val artworkUrl: String,
    val artworkSize: ULong = 0U,
    val length: Double,
    val introStartTime: Double?,
    val introEndTime: Double?,
    val creditsStartTime: Double?,
    val bifUrl: String,
    val bifSize: ULong = 0U,
    val videoUrl: String,
    val videoSize: ULong = 0U,
    val srtSubtitles: List<SRTSubtitles>?
)