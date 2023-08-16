package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

/*
Possible Bit Flags:

    0x0000 = None
    0x0001 = G
    0x0002 = PG
    0x0004 = PG-13
    0x0008 = R
    0x0010 = NC-17
    0x0020 = Unrated
    0x0040 = TV-Y
    0x0080 = TV-Y7
    0x0100 = TV-G
    0x0200 = TV-PG
    0x0400 = TV-14
    0x0800 = TV-MA
    0x1000 = Not Rated

*/

enum class Ratings {

    @SerializedName("0") None,

    @SerializedName("1") G,
    @SerializedName("2") PG,
    @SerializedName("4") PG_13,
    @SerializedName("8") R,
    @SerializedName("16") NC_17,
    @SerializedName("32") Unrated,

    @SerializedName("64") TV_Y,
    @SerializedName("128") TV_Y7,
    @SerializedName("256") TV_G,
    @SerializedName("512") TV_PG,
    @SerializedName("1024") TV_14,
    @SerializedName("2048") TV_MA,

    @SerializedName("4096") NotRated
}