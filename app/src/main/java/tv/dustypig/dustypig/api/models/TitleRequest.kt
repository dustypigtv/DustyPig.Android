package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class TitleRequest(
    @SerializedName("tmdb_id") val tmdbId: Int,
    @SerializedName("friend_id") val friendId: Int? = null,
    @SerializedName("media_type") val mediaType: TMDBMediaTypes
)
