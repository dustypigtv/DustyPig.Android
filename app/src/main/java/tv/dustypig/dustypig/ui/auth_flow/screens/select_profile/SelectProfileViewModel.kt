package tv.dustypig.dustypig.ui.auth_flow.screens.select_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.LoginTypes
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.repositories.AuthRepository
import tv.dustypig.dustypig.api.repositories.ProfilesRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.global_managers.fcm_manager.FCMManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject


@HiltViewModel
class SelectProfileViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val authRepository: AuthRepository,
    private val authManager: AuthManager,
    private val profilesRepository: ProfilesRepository,
    private val settingsManager: SettingsManager
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        SelectProfileUIState(
            onPopBackStack = ::popBackStack,
            onHideError = ::hideError,
            onSignIn = ::signIn
        )
    )
    val uiState: StateFlow<SelectProfileUIState> = _uiState.asStateFlow()


    private var criticalError = false

    init {
        viewModelScope.launch {
            try {
                val data = profilesRepository.list()
                _uiState.update { it.copy(busy = false, profiles = data) }
            } catch (ex: Exception) {
                setError(ex = ex, isCritical = true)
            }
        }
    }

    private fun setError(ex: Exception, isCritical: Boolean) {
        criticalError = isCritical
        _uiState.update {
            it.copy(
                busy = false,
                showError = true,
                errorMessage = ex.localizedMessage,
            )
        }
        ex.logToCrashlytics()
    }

    private fun hideError() {
        _uiState.update { it.copy(showError = false) }
        if(criticalError)
            popBackStack()
    }


    private fun signIn(profileId: Int, pin: UShort?) {
        _uiState.update {
            it.copy(
                busy = true
            )
        }
        viewModelScope.launch {
            try{
                val data = authRepository.profileLogin(
                    ProfileCredentials(
                        profileId,
                        pin?.toInt(),
                        FCMManager.currentToken,
                        settingsManager.getDeviceId()
                    )
                )
                authManager.setAuthState(
                    data.profileToken!!,
                    data.profileId!!,
                    data.loginType == LoginTypes.MainProfile
                )
            } catch (ex: Exception) {
                setError(ex = ex, isCritical = false)
            }
        }
    }
}
