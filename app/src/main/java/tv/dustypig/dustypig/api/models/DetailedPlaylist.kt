package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedPlaylist (
    val id: Int = 0,
    val name: String = "",
    @SerializedName("artwork_url") val artworkUrl: String = "",
    @SerializedName("current_item_id") val currentItemId: Int = 0,
    @SerializedName("current_progress") val currentProgress: Double = 0.0,
    var items: List<PlaylistItem>? = null
)