package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class MovePlaylistItem (
    val id: Int,
    @SerializedName("new_index") val newIndex: Int
)