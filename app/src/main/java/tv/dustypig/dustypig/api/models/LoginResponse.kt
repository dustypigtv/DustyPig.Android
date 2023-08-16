package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName


class LoginResponse (
    @SerializedName("login_type") val loginType: LoginTypes,
    val token: String?,
    @SerializedName("profile_id") val profileId: Int?
)