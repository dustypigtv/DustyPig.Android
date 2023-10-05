package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class BasicLibrary (
    val id: Int,
    val name: String,
    @SerializedName("is_tv") val isTV: Boolean
)