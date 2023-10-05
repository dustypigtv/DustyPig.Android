package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class DetailedLibrary(
    val id: Int,
    val name: String,
    @SerializedName("is_tv") val isTV: Boolean,
    val profiles: List<BasicProfile>,
    @SerializedName("shared_with") val sharedWith: List<BasicFriend>
)
