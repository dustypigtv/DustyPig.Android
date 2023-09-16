package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class SearchResults (
    val available: List<BasicMedia>?,
    @SerializedName("other_titles_allowed") val otherTitlesAllowed: Boolean,
    @SerializedName("other_titles") val otherTitles: List<BasicTMDB>?
)