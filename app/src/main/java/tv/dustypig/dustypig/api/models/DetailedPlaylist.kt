package tv.dustypig.dustypig.api.models

data class DetailedPlaylist (
    val id: Int = 0,
    val name: String = "",
    val artworkUrl: String = "",
    val artworkSize: ULong = 0U,
    val currentItemId: Int = 0,
    val currentProgress: Double = 0.0,
    var items: List<PlaylistItem>? = null
)