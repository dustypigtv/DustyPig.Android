package tv.dustypig.dustypig.api.models

data class SearchResults (
    val available: List<BasicMedia>?,
    val otherTitlesAllowed: Boolean,
    val otherTitles: List<BasicTMDB>?,
    val availablePeople: List<BasicPerson>?,
    val otherPeople: List<BasicPerson>?
)