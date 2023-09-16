package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedTMDB (
    @SerializedName("tmdb_id") val tmdbId: Int,
    @SerializedName("media_type") val mediaType: TMDB_MediaTypes,
    @SerializedName("artwork_url") val artworkUrl: String?,
    @SerializedName("backdrop_url") val backdropUrl: String?,
    val title: String,
    val year: Int,
    val rated: String?,
    val genres: Long,
    val cast: List<String>?,
    val directors: List<String>?,
    val producers: List<String>?,
    val writers: List<String>?,
    val description: String?,
    val available: List<BasicMedia>?,
    val requestPermissions: TitleRequestPermissions?,
    val requestStatus: RequestStatus?
)
