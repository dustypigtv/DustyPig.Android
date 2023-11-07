package tv.dustypig.dustypig.ui.main_app.screens.settings.switch_profiles

import tv.dustypig.dustypig.api.models.BasicProfile

data class SwitchProfilesUIState(

    //Data
    val busy: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val profiles: List<BasicProfile> = listOf(),

    //Events
    val onPopBackStack: () -> Unit = { },
    val onHideError: () -> Unit = { },
    val onSignIn: (profile: BasicProfile, pin: UShort?) -> Unit = { _, _ -> }
)
