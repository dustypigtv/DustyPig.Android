package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class HomeScreenList(
    @SerializedName("list_id") val listId: Long,
    val title: String,
    val items: List<BasicMedia>
)