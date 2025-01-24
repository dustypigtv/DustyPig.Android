package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

enum class MovieRatings {
    @SerializedName("0")
    None,
    @SerializedName("1")
    G,
    @SerializedName("2")
    PG,
    @SerializedName("3")
    PG13 {
        override fun toString() = "PG-13"
    },
    @SerializedName("4")
    R,
    @SerializedName("5")
    NC17 {
        override fun toString() = "NC-17"
    },
    @SerializedName("6")
    Unrated
}