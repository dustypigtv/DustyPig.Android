package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class ProfileLibraryLink(
    @SerializedName("profile_id") val profileId: Int,
    @SerializedName("library_id") val libraryId: Int
)
