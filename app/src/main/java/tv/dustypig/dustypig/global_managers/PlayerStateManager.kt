package tv.dustypig.dustypig.global_managers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID


object PlayerStateManager {

   var playerScreenVisible by mutableStateOf(false)
        private set

    private val _playbackEnded = MutableStateFlow(UUID.randomUUID())
    val playbackEnded = _playbackEnded.asStateFlow()

    fun playerCreated() {
        playerScreenVisible = true
    }

    fun playerDisposed() {
        playerScreenVisible = false
        _playbackEnded.update { UUID.randomUUID() }
    }
}