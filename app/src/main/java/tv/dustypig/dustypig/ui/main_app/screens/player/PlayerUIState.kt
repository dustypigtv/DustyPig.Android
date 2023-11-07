package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.media3.common.Player
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager

data class PlayerUIState (

    //Data
    val busy: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val player: Player? = null,
    val currentPositionWithinIntro: Boolean = false,
    val currentPositionWithinCredits: Boolean = false,
    val currentItemTitle: String? = null,
    val isCastPlayer: Boolean = false,
    val castManager: CastManager,
    val castPaused: Boolean = false,
    val castBuffering: Boolean = false,
    val castHasPrevious: Boolean = false,
    val castHasNext: Boolean = false,
    val castPosition: Float = 0f,
    val castDuration: Float = 0f,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onSkipIntro: () -> Unit = { },
    val onPlayNext: () -> Unit = { }
)