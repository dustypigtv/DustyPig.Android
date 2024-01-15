package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.media3.common.Player
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastPlaybackStatus

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
    val castPlaybackStatus: CastPlaybackStatus = CastPlaybackStatus.Stopped,
    val castHasPrevious: Boolean = false,
    val castHasNext: Boolean = false,
    val castPosition: Long = 0L,
    val castDuration: Long = 0L,


    //Events
    val onPopBackStack: () -> Unit = { },
    val onSkipIntro: () -> Unit = { },
    val onPlayNext: () -> Unit = { }
)