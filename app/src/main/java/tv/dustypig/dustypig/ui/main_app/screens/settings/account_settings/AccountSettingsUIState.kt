package tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings

data class AccountSettingsUIState(
    val busy: Boolean = true,
    val isMainProfile: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val loginToDeviceSuccess: Boolean = false,
    val showChangePasswordSuccessAlert: Boolean = false,
)
