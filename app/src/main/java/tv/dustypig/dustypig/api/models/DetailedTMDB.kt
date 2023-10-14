package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedTMDB (
    @SerializedName("tmdb_id") val tmdbId: Int,
    @SerializedName("media_type") val mediaType: TMDBMediaTypes = TMDBMediaTypes.Movie,
    @SerializedName("artwork_url") val artworkUrl: String?,
    @SerializedName("backdrop_url") val backdropUrl: String?,
    val title: String,
    val year: Int,
    val rated: String?,
    val description: String?,
    val genres: Long = 0,
    val cast: List<String> = listOf(),
    val directors: List<String> = listOf(),
    val producers: List<String> = listOf(),
    val writers: List<String> = listOf(),
    val available: List<BasicMedia> = listOf(),
    @SerializedName("request_permission") val requestPermissions: TitleRequestPermissions = TitleRequestPermissions.Enabled,
    @SerializedName("request_status") val requestStatus: RequestStatus = RequestStatus.NotRequested
)
