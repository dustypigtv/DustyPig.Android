package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.media3.common.Player

data class PlayerUIState (
    val busy: Boolean = false,
    val player: Player? = null
)