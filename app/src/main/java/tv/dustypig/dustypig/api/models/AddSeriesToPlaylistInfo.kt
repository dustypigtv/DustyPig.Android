package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class AddSeriesToPlaylistInfo(
    @SerializedName("playlist_id") val playlistId: Int,
    @SerializedName("media_id") val mediaId: Int,
    @SerializedName("auto_add_new_episodes") val autoAddNewEpisodes: Boolean
)
