package tv.dustypig.dustypig.ui.main_app.screens.settings

data class SettingsUIState(

    //Data
    val links: List<SettingLink> = listOf(),

    //Event
    val onNavToRoute: (route: String) -> Unit = { },
)


data class SettingLink(
    val resourceId: Int,
    val route: String
)