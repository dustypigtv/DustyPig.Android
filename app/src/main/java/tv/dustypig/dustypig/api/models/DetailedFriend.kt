package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class DetailedFriend(
    val id: Int,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    val accepted: Boolean,
    val timestamp: Date,
    @SerializedName("shared_with_friend") val libsSharedWithFriend: List<BasicLibrary>,
    @SerializedName("shared_with_me") val libsSharedWithMe: List<BasicLibrary>
)
