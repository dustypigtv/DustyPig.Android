package tv.dustypig.dustypig.ui.main_app.screens.settings

data class SettingsUIState(

    //Data
    val isMainProfile: Boolean = false,

    //Event
    val onNavToRoute: (route: String) -> Unit = { },
    val onNavToMyProfile: () -> Unit = { }
)
