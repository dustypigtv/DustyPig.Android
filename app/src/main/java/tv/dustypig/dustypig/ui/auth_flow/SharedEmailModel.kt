package tv.dustypig.dustypig.ui.auth_flow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


// This will be shred between 3 composable objects, so it's singleton
object SharedEmailModel  {
    private val _uiState = MutableStateFlow(EmailUIState())
    val uiState: StateFlow<EmailUIState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update {
            it.copy(
                email = email.trim().lowercase()
            )
        }
    }
}

data class EmailUIState(
    val email: String = ""
)