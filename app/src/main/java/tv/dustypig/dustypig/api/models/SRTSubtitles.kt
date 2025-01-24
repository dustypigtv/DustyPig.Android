package tv.dustypig.dustypig.api.models

data class SRTSubtitles(
    val name: String,
    val url: String,
    val fileSize: ULong = 0U
)