package tv.dustypig.dustypig.api.models

data class DetailedPlaylist(
    val id: Int = 0,
    val name: String = "",
    val artworkUrl: String = "",
    val backdropUrl: String = "",
    val artworkSize: ULong = 0U,
    var currentItemId: Int = 0,
    var currentProgress: Double = 0.0,
    var items: List<PlaylistItem>? = null
)