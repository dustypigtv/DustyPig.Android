package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

enum class MediaTypes {
    @SerializedName("1")
    Movie,
    @SerializedName("2")
    Series,
    @SerializedName("3")
    Episode,
    @SerializedName("4")
    Playlist;

    companion object {
        fun getByVal(value: String?): MediaTypes? = when (value) {
            "1" -> Movie
            "2" -> Series
            "3" -> Episode
            "4" -> Playlist
            else -> null
        }
    }
}