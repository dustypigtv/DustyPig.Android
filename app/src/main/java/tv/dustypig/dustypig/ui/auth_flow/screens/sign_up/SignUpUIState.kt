package tv.dustypig.dustypig.ui.auth_flow.screens.sign_up



data class SignUpUIState(

    //Data
    val email: String = "",
    val showSuccess: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val message: String = "",
    val busy: Boolean = false,

    //Events
    val onHideError: () -> Unit = { },
    val onNavToSignIn: (email: String) -> Unit = { },
    val onSignUp: (name: String, email: String, password: String) -> Unit = { _, _, _ -> }

)