package tv.dustypig.dustypig.api.models

import java.util.Date

data class DetailedFriend(
    val id: Int,
    val displayName: String,
    val avatarUrl: String,
    val accepted: Boolean,
    val timestamp: Date,
    val sharedWithFriend: List<BasicLibrary>,
    val sharedWithMe: List<BasicLibrary>
)
