package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class AddPlaylistItem(
    @SerializedName("playlist_id") val playlistId: Int,
    @SerializedName("media_id") val mediaId: Int
)
