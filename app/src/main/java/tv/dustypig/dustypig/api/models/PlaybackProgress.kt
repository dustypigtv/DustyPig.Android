package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class PlaybackProgress(
    @SerializedName("id") val id: Int,
    @SerializedName("seconds") val seconds: Double
)