package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class ExternalSubtitle (
    val name: String,
    val url: String,
    @SerializedName("file_size") val fileSize: ULong = 0U
)