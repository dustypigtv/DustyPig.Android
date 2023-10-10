package tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings

import tv.dustypig.dustypig.api.models.BasicProfile

data class ProfilesSettingsUIState(
    val busy: Boolean = true,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val profiles: List<BasicProfile> = listOf()
)
