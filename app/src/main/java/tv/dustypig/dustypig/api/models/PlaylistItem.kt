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
    val backdropUrl: String?,
    val length: Double,
    val introStartTime: Double?,
    val introEndTime: Double?,
    val creditsStartTime: Double?,
    val bifUrl: String,
    val videoUrl: String,
    val srtSubtitles: List<SRTSubtitles>?
)