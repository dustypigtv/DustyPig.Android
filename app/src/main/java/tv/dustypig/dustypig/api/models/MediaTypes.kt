package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

enum class MediaTypes {
    @SerializedName("1") Movie,
    @SerializedName("2") Series,
    @SerializedName("3") Episode,
    @SerializedName("4") Playlist
}