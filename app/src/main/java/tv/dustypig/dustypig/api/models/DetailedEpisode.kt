package tv.dustypig.dustypig.api.models

data class DetailedEpisode(
    val id: Int = 0,
    val bifUrl: String? = null,
    val videoUrl: String = "",
    val srtSubtitles: List<SRTSubtitles>? = null,
    var played: Double? = null,
    var upNext: Boolean = false,
    val title: String = "",
    val description: String? = null,
    val artworkUrl: String = "",
    val length: Double = 0.0,
    val introStartTime: Double? = null,
    val introEndTime: Double? = null,
    val creditsStartTime: Double? = null,
    val seasonNumber: UShort = 0u,
    val episodeNumber: UShort = 0u,
    val seriesId: Int = 0,
    var seriesTitle: String? = null
) {
    fun fullDisplayTitle() = "S$seasonNumber:E$episodeNumber - $title"
    fun shortDisplayTitle() = "E$episodeNumber: $title"
}
