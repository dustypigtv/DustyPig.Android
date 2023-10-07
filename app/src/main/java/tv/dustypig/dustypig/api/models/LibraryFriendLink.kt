package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class LibraryFriendLink(
    @SerializedName("library_id") val libraryId: Int,
    @SerializedName("friend_id") val friendId: Int
)
