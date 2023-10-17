package tv.dustypig.dustypig.ui.main_app.screens.player

data class PlayerUIState (
    val busy: Boolean = false,
    val playbackItems: List<PlaybackItem> = listOf()
)