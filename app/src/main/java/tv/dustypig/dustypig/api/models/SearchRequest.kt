package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class SearchRequest (
    val query: String,
    @SerializedName("search_tmdb") val searchTMDB: Boolean
)