package tv.dustypig.dustypig.ui.auth_flow.screens.sign_up



data class SignUpUIState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val showSuccess: Boolean = false,
    val showError: Boolean = false,
    val message: String = "",
    val busy: Boolean = false
)