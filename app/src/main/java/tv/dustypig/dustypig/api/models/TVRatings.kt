package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

enum class TVRatings {
    @SerializedName("0")
    None,
    @SerializedName("1")
    Y {
        override fun toString() = "TV-Y"
    },
    @SerializedName("2")
    Y7 {
        override fun toString() = "TV-Y7"
    },
    @SerializedName("3")
    G {
        override fun toString() = "TV-G"
    },
    @SerializedName("4")
    PG {
        override fun toString() = "TV-PG"
    },
    @SerializedName("5")
    TV_14 {
        override fun toString() = "TV-14"
    },
    @SerializedName("6")
    MA {
        override fun toString() = "TV-MA"
    },
    @SerializedName("7")
    NotRated {
        override fun toString() = "Not Rated"
    }
}