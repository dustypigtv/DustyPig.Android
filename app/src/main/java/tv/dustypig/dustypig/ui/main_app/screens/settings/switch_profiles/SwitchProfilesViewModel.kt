package tv.dustypig.dustypig.ui.main_app.screens.settings.switch_profiles

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.api.models.LoginTypes
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.repositories.AuthRepository
import tv.dustypig.dustypig.api.repositories.ProfilesRepository
import tv.dustypig.dustypig.global_managers.AlertsManager
import tv.dustypig.dustypig.global_managers.auth_manager.AuthManager
import tv.dustypig.dustypig.global_managers.FCMManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import javax.inject.Inject

@HiltViewModel
class SwitchProfilesViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    private val profilesRepository: ProfilesRepository,
    private val authRepository: AuthRepository,
    private val authManager: AuthManager,
    private val settingsManager: SettingsManager
) : ViewModel(), RouteNavigator by routeNavigator {

    companion object {
        private const val TAG = "SwitchProfilesViewModel"
    }

    private val _uiState = MutableStateFlow(
        SwitchProfilesUIState(
            onPopBackStack = ::popBackStack,
            onHideError = ::hideError,
            onSignIn = ::signIn
        )
    )
    val uiState = _uiState.asStateFlow()

    private var criticalError = false

    init {
        viewModelScope.launch {
            try {
                val data = profilesRepository.list()
                _uiState.update { it.copy(busy = false, profiles = data) }
            } catch (ex: Exception) {
                criticalError = true
                setError(ex)
            }
        }
    }

    private fun setError(ex: Exception) {
        ex.logToCrashlytics()
        _uiState.update {
            it.copy(
                busy = false,
                showError = true,
                errorMessage = ex.localizedMessage
            )
        }
    }

    private fun hideError() {
        if (criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showError = false)
            }
        }
    }

    private fun signIn(profile: BasicProfile, pin: UShort?) {
        _uiState.update {
            it.copy(busy = true)
        }
        viewModelScope.launch {
            try {
                val allowNotifications = settingsManager.getAllowNotifications(profile.id)

                val fcmToken: String? =
                    if (allowNotifications)
                        FCMManager.currentToken
                    else
                        null

                Log.d(TAG, "Pre login")
                val data = authRepository.profileLogin(
                    ProfileCredentials(
                        profile.id,
                        pin?.toInt(),
                        fcmToken
                    )
                )
                Log.d(TAG, "Post login")

                if (!allowNotifications)
                    FCMManager.resetToken()


                authManager.switchProfile(
                    data.profileToken!!,
                    data.profileId!!,
                    data.loginType == LoginTypes.MainProfile
                )

            } catch (ex: Exception) {
                criticalError = false
                setError(ex)
            }
        }
    }
}








