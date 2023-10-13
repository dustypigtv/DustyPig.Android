package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class BasicTMDB (
    @SerializedName("tmdb_id") val tmdbId: Int,
    @SerializedName("media_type") val mediaType: TMDBMediaTypes,
    @SerializedName("artwork_url") val artworkUrl: String?,
    @SerializedName("backdrop_url") val backdropUrl: String?,
    val title: String
)