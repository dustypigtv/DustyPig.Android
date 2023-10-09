package tv.dustypig.dustypig.ui.main_app.screens.settings.my_profile_settings

data class MyProfileSettingsUIState(
    val busy: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val name: String = "",
    val avatarUrl: String = ""
)
