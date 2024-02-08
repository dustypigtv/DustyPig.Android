package tv.dustypig.dustypig.api.models

data class AddSeriesToPlaylistInfo(
    val playlistId: Int,
    val mediaId: Int,
    val autoAddNewEpisodes: Boolean
)
