package tv.dustypig.dustypig.api.models

data class LoadMoreHomeScreenItemsRequest (
    val listId: Long,
    val start: Int
)