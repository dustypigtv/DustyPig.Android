package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.media3.common.Player

data class PlayerUIState (
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val player: Player? = null,
    val currentItemHasSubtitles: Boolean = false,
    val currentPositionWithinIntro: Boolean = false,
    val currentPositionWithinCredits: Boolean = false,
    val currentItemTitle: String? = null
)