package tv.dustypig.dustypig.api.models


class LoginResponse (
    val login_type: Int,
    val token: String?,
    val profile_id: Int?
) {
    companion object {
        const val LOGIN_TYPE_ACCOUNT: Int = 0
        const val LOGIN_TYPE_MAIN_PROFILE: Int = 1
        const val LOGIN_TYPE_SUB_PROFILE: Int = 2
    }
}