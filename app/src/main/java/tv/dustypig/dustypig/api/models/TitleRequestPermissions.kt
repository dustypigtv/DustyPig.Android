package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

enum class TitleRequestPermissions() {
    @SerializedName("0")
    Enabled,

    @SerializedName("1")
    Disabled,

    @SerializedName("2")
    RequiresAuthorization {
        override fun toString() = "Requires Permission"
    }
}

