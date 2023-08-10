package tv.dustypig.dustypig.api.models

data class HomeScreenList(
    val list_id: Long,
    val title: String,
    val items: List<BasicMedia>
)