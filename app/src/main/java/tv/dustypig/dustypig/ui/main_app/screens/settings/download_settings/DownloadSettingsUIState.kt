package tv.dustypig.dustypig.ui.main_app.screens.settings.download_settings

data class DownloadSettingsUIState(

    //Data
    val downloadOverMobile: Boolean = false,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onSetDownloadOverMobile: (value: Boolean) -> Unit = { }
)
