package tv.dustypig.dustypig.ui.auth_flow.screens.select_profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.AuthManager
import tv.dustypig.dustypig.api.ThePig
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.api.models.LoginResponse
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.throwIfError
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject


@HiltViewModel
class SelectProfileViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(SelectProfileUIState())
    val uiState: StateFlow<SelectProfileUIState> = _uiState.asStateFlow()

    private var _loadingError = false
    private var _profileId: Int = 0


    init {
        viewModelScope.launch {
            try {
                val response = ThePig.api.listProfiles()
                response.throwIfError()
                _uiState.update { it.copy(busy = false, profiles = response.body()!!.data) }
            } catch (ex: Exception) {
                _loadingError = true
                _uiState.update { it.copy(busy = false, showError = true, errorMessage = ex.localizedMessage ?: "Unknown Error") }
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

    private fun profileSignIn(pin: Int?, context: Context) {
        _uiState.update { it.copy(busy = true, showPinDialog = false) }
        viewModelScope.launch {
            try{
                val response = ThePig.api.profileLogin(ProfileCredentials(_profileId, pin, null))
                response.throwIfError()
                val data = response.body()!!.data
                AuthManager.setAuthState(context, data.token!!, data.profile_id!!, data.login_type == LoginResponse.LOGIN_TYPE_MAIN_PROFILE)
            } catch (ex: Exception) {
                _loadingError = false
                _uiState.update { it.copy(busy = false, showError = true, errorMessage = ex.localizedMessage ?: "Unknown Error") }
            }
        }
    }

    fun onProfileSelected(profile: BasicProfile, context: Context) {
        _profileId = profile.id
        if(profile.has_pin) {
            _uiState.update { it.copy(showPinDialog = true) }
        }
        else {
            profileSignIn(null, context)
        }
    }

    fun onPinSubmitted(context: Context) {
        val pin = try {
            Integer.parseUnsignedInt(uiState.value.pin)
        } catch (_: Exception) {
            null
        }

        profileSignIn(pin, context)
    }

}


data class SelectProfileUIState(
    val busy: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val showPinDialog: Boolean = false,
    val pin: String = "",
    val profiles: List<BasicProfile> = List(0) { BasicProfile() }
)