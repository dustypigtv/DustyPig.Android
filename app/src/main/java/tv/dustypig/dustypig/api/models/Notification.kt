package tv.dustypig.dustypig.api.models

import java.util.Date

data class Notification(
    val id: Int,
    val profileId: Int,
    val title: String,
    val message: String,
    val notificationType: NotificationTypes,
    val mediaType: MediaTypes?,
    val friendshipId: Int?,
    var seen: Boolean,
    val timestamp: Date,

    // For new media requests, this will be the TMDB id.
    // For new movie/series fulfilled notifications, this will be the media id in the database.
    // For new episode available notifications, this will be the media id of the series in the database.
    // For all others, this will be null.
    val mediaId: Int?
)
