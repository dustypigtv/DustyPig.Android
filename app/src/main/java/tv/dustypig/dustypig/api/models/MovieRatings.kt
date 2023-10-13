package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

enum class MovieRatings {
    @SerializedName("0") None,
    @SerializedName("1") Movies_G { override fun toString() = "G" },
    @SerializedName("2") Movies_PG { override fun toString() = "PG" },
    @SerializedName("3") Movies_PG13 { override fun toString() = "PG-13" },
    @SerializedName("4") Movies_R { override fun toString() = "R" },
    @SerializedName("5") Movies_NC17 { override fun toString() = "NC-17" },
    @SerializedName("6") Movies_Unrated { override fun toString() = "Unrated" }
}