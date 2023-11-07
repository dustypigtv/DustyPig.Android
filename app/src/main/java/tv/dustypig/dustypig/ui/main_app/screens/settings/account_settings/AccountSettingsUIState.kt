package tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings

data class AccountSettingsUIState(

    //Data
    val busy: Boolean = true,
    val isMainProfile: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val loginToDeviceSuccess: Boolean = false,
    val showChangePasswordSuccessAlert: Boolean = false,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onHideError: () -> Unit = { },
    val onSignOut: () -> Unit = { },
    val onLoginToDevice: (code: String) -> Unit = { },
    val onChangePassword: (newPassword: String) -> Unit = { },
    val onHideChangePasswordDialog: () -> Unit = { },
    val onSignoutEverywhere: () -> Unit = { },
    val onDeleteAccount: () -> Unit = { }
)
