package tv.dustypig.dustypig.api.models

data class LoadMoreHomeScreenItemsRequest (
    val list_id: Long,
    val start: Int
)