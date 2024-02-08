package tv.dustypig.dustypig.api.models

data class DetailedLibrary(
    val id: Int,
    val name: String,
    val isTV: Boolean,
    val profiles: List<BasicProfile>,
    val sharedWith: List<BasicFriend>
)
