package tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings

data class AccountSettingsUIState(
    val busy: Boolean = true,
    val isMainProfile: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val showLoginToDeviceDialog: Boolean = false,
    val showLoginToDeviceAlert: Boolean = false,
    val loginToDeviceSuccess: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val showChangePasswordSuccessAlert: Boolean = false,
    val showSignoutEverywhereDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false
)
