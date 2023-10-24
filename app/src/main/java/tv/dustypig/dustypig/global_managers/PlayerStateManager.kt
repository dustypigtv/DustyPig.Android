package tv.dustypig.dustypig.global_managers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

object PlayerStateManager {

    private val _playerScreenVisible = MutableStateFlow(false)
    val playerScreenVisible = _playerScreenVisible.asStateFlow()

    private val _playbackEnded = MutableStateFlow(UUID.randomUUID())
    val playbackEnded = _playbackEnded.asStateFlow()


    fun playerCreated() {
        _playerScreenVisible.update {
            true
        }
    }

    fun playerDisposed() {
        _playerScreenVisible.update {
            false
        }
        _playbackEnded.update {
            UUID.randomUUID()
        }
    }
}