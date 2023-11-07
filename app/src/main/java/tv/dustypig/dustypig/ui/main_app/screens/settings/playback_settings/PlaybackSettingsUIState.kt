package tv.dustypig.dustypig.ui.main_app.screens.settings.playback_settings

data class PlaybackSettingsUIState(

    //Data
    val autoSkipIntros: Boolean = false,
    val autoSkipCredits: Boolean = false,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onSetAutoSkipIntros: (value: Boolean) -> Unit = { },
    val onSetAutoSkipCredits: (value: Boolean) -> Unit = { }
)
