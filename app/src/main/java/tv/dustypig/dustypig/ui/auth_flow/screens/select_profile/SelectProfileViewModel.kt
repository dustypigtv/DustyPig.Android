package tv.dustypig.dustypig.ui.auth_flow.screens.select_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.FCMManager
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.api.models.LoginTypes
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.repositories.AuthRepository
import tv.dustypig.dustypig.api.repositories.ProfilesRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject


@HiltViewModel
class SelectProfileViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val authRepository: AuthRepository,
    private val authManager: AuthManager,
    private val profilesRepository: ProfilesRepository
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(SelectProfileUIState())
    val uiState: StateFlow<SelectProfileUIState> = _uiState.asStateFlow()

    private var _loadingError = false
    private var _profileId: Int = 0


    init {
        viewModelScope.launch {
            try {
                val data = profilesRepository.list()
                _uiState.update { it.copy(busy = false, profiles = data) }
            } catch (ex: Exception) {
                ex.logToCrashlytics()
                _loadingError = true
                _uiState.update { it.copy(busy = false, showError = true, errorMessage = ex.localizedMessage) }
            }
        }
    }

    fun updatePin(pin: String) {
        _uiState.update { it.copy(pin = pin) }
    }

    fun hideError() {
        _uiState.update { it.copy(showError = false) }
        if(_loadingError)
            popBackStack()
    }

    fun cancelPinDialog() {
        _uiState.update { it.copy(showPinDialog = false) }
    }

    private fun profileSignIn(pin: Int?) {
        _uiState.update { it.copy(busy = true, showPinDialog = false) }
        viewModelScope.launch {
            try{
                val data = authRepository.profileLogin(ProfileCredentials(_profileId, pin, FCMManager.currentToken))
                authManager.setAuthState(data.token!!, data.profileId!!, data.loginType == LoginTypes.MainProfile)
            } catch (ex: Exception) {
                ex.logToCrashlytics()
                _loadingError = false
                _uiState.update { it.copy(busy = false, showError = true, errorMessage = ex.localizedMessage) }
            }
        }
    }

    fun onProfileSelected(profile: BasicProfile) {
        _profileId = profile.id
        if(profile.hasPin) {
            _uiState.update { it.copy(showPinDialog = true) }
        }
        else {
            profileSignIn(null)
        }
    }

    fun onPinSubmitted() {
        val pin = try {
            Integer.parseUnsignedInt(uiState.value.pin)
        } catch (ex: Exception) {
            ex.logToCrashlytics()
            null
        }

        profileSignIn(pin)
    }

}
