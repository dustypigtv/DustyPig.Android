package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

enum class LoginTypes {
    @SerializedName("0")
    Account,
    @SerializedName("1")
    MainProfile,
    @SerializedName("2")
    SubProfile
}