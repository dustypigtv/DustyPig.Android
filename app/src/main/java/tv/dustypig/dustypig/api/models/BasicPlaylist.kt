package tv.dustypig.dustypig.api.models

data class BasicPlaylist(
    val id: Int,
    val name: String,
    val artworkUrl: String,
    val backdropUrl: String = DEFAULT_BACKDROP
) {
    companion object {
        const val DEFAULT_ARTWORK = "https://s3.dustypig.tv/user-art/playlist/default.png"
        const val DEFAULT_BACKDROP = "https://s3.dustypig.tv/user-art/playlist/default.backdrop.png"
    }
}
