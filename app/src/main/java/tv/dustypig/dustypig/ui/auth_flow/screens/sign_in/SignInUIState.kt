package tv.dustypig.dustypig.ui.auth_flow.screens.sign_in


data class SignInUIState(
    val busy: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val showForgotPassword: Boolean = false,
    val forgotPasswordBusy: Boolean = false,
    val showForgotPasswordSuccess: Boolean = false,
    val showForgotPasswordError: Boolean = false
)