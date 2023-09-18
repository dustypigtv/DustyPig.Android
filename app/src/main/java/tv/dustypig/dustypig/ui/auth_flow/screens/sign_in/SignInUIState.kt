package tv.dustypig.dustypig.ui.auth_flow.screens.sign_in


data class SignInUIState(
    val email: String = "",
    val password: String = "",
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val busy: Boolean = false,
    val showForgotPassword: Boolean = false,
    val forgotPasswordBusy: Boolean = false,
    val showForgotPasswordSuccess: Boolean = false,
    val showForgotPasswordError: Boolean = false
)