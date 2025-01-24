package tv.dustypig.dustypig.api.models

data class SearchRequest(
    val query: String,
    val searchTMDB: Boolean
)