package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedProfile(
    val id: Int,
    val name: String,
    val locked: Boolean,
    @SerializedName("has_pin") val hasPin: Boolean,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("max_movie_rating") val maxMovieRating: MovieRatings,
    @SerializedName("max_tv_rating") val maxTVRating: TVRatings,
    @SerializedName("title_request_permissions") val titleRequestPermissions: TitleRequestPermissions,
    @SerializedName("available_libraries") val availableLibraries: List<BasicLibrary>
)

