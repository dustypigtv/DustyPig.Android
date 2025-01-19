package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

/*
public enum NotificationTypes
{
    NewMediaRequested = 1,
    NewMediaPending = 2,
    NewMediaFulfilled = 3,
    NewMediaRejected = 4,
    NewMediaAvailable = 5,

    OverrideMediaRequested = 6,
    OverrideMediaGranted = 7,
    OverrideMediaRejected = 8,

    FriendshipInvited = 9,
    FriendshipAccepted = 10
}
*/

enum class NotificationTypes {

    @SerializedName("1") NewMediaRequested,
    @SerializedName("2") NewMediaPending,
    @SerializedName("3") NewMediaFulfilled,
    @SerializedName("4") NewMediaRejected,
    @SerializedName("5") NewMediaAvailable,
    @SerializedName("6") OverrideMediaRequested,
    @SerializedName("7") OverrideMediaGranted,
    @SerializedName("8") OverrideMediaRejected,
    @SerializedName("9") FriendshipInvited,
    @SerializedName("10") FriendshipAccepted;

    companion object {
        fun getByVal(value: String?): NotificationTypes? = when(value) {
            "1" -> NewMediaRejected
            "2" -> NewMediaPending
            "3" -> NewMediaFulfilled
            "4" -> NewMediaRejected
            "5" -> NewMediaAvailable
            "6" -> OverrideMediaRequested
            "7" -> OverrideMediaGranted
            "8" -> OverrideMediaRejected
            "9" -> FriendshipInvited
            "10" -> FriendshipAccepted
            else -> null
        }
    }
}
