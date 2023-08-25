package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class BasicPlaylist(
    val id: Int,
    val name: String,
    @SerializedName("artwork_url") val artworkUrl: String
)
