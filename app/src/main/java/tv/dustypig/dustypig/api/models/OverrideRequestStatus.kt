package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

enum class OverrideRequestStatus {
    @SerializedName("0")
    NotRequested,
    @SerializedName("1")
    Requested,
    @SerializedName("2")
    Denied,
    @SerializedName("3")
    Granted
}