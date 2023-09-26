package tv.dustypig.dustypig.ui.auth_flow.screens.sign_up



data class SignUpUIState(
    val email: String = "",
    val showSuccess: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val message: String = "",
    val busy: Boolean = false
)