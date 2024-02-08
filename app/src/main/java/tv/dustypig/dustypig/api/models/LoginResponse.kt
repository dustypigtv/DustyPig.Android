package tv.dustypig.dustypig.api.models


class LoginResponse (
    val loginType: LoginTypes,
    val token: String?,
    val profileId: Int?
)