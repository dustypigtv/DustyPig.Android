package tv.dustypig.dustypig.api.models

data class HomeScreenList(
    val listId: Long,
    val title: String,
    val items: List<BasicMedia>
)