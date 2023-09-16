package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class BasicMedia (
    val id: Int,
    @SerializedName("media_type") val mediaType: MediaTypes,
    @SerializedName("artwork_url") val artworkUrl: String,
    @SerializedName("backdrop_url") val backdropUrl: String?,
    val title: String
)