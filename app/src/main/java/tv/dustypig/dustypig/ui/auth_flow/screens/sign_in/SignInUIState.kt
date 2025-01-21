package tv.dustypig.dustypig.ui.auth_flow.screens.sign_in


data class SignInUIState(

    //Data
    val emailAddress: String = "",
    val busy: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val forgotPasswordBusy: Boolean = false,
    val showForgotPasswordSuccess: Boolean = false,
    val showForgotPasswordError: Boolean = false,

    //Events
    val onHideError: () -> Unit = { },
    val onHideForgotPasswordSuccess: () -> Unit = { },
    val onHideForgotPasswordError: () -> Unit = { },
    val onSignIn: (String, String) -> Unit = { _, _ -> },
    val onSendForgotPasswordEmail: (String) -> Unit = { },
    val onNavToSignUp: (String) -> Unit = { }
)