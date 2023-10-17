package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class SetPlaylistProgress(
    @SerializedName("playlist_id") val playlistId: Int,
    @SerializedName("new_index") val newIndex: Int,
    @SerializedName("new_progress") val newProgress: Double
)
