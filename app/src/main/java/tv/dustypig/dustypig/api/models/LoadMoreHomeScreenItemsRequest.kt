package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class LoadMoreHomeScreenItemsRequest (
    @SerializedName("list_id") val listId: Long,
    val start: Int
)