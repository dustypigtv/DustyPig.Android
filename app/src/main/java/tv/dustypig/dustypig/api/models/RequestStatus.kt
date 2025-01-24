package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

enum class RequestStatus {
    @SerializedName("0")
    NotRequested,
    @SerializedName("1")
    RequestSentToMain,
    @SerializedName("2")
    RequestSentToAccount,
    @SerializedName("3")
    Denied,
    @SerializedName("4")
    Pending,
    @SerializedName("5")
    Fulfilled
}
