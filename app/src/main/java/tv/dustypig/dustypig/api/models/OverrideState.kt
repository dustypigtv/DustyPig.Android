package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

enum class OverrideState {

    //Default is used by the server only. If you try to set it in a call to setTitlePermissions, the server will return a validation error
    //@SerializedName("0") Default,

    @SerializedName("1") Allow,
    @SerializedName("2") Block
}