package tv.dustypig.dustypig.api.models

data class BasicMedia (
    val id: Int,
    val mediaType: MediaTypes,
    val artworkUrl: String,
    val backdropUrl: String?,
    val title: String
)