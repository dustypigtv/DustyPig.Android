package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedPlaylist (
    val id: Int,
    val name: String,
    @SerializedName("artwork_url") val artworkUrl: String,
    @SerializedName("current_index") val currentIndex: Int,
    var items: List<PlaylistItem>?
)