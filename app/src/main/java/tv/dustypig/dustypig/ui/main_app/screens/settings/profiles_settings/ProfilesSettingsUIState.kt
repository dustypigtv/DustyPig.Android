package tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings

import tv.dustypig.dustypig.api.models.BasicProfile

data class ProfilesSettingsUIState(

    //Data
    val busy: Boolean = true,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val profiles: List<BasicProfile> = listOf(),

    //Events
    val onPopBackStack: () -> Unit = { },
    val onHideError: () -> Unit = { },
    val onNavToAddProfile: () -> Unit = { },
    val onNavToProfile: (id: Int) -> Unit = { }
)
