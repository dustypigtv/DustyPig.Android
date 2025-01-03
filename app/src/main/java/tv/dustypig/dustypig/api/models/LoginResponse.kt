package tv.dustypig.dustypig.api.models


class LoginResponse (
    val loginType: LoginTypes,
    val accountToken: String? = null,
    val profileToken: String? = null,
    val profileId: Int? = null
)