package tv.dustypig.dustypig.ui.auth_flow.screens.sign_in

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.FCMToken
import tv.dustypig.dustypig.api.models.LoginTypes
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.repositories.AuthRepository
import tv.dustypig.dustypig.global_managers.auth_manager.AuthManager
import tv.dustypig.dustypig.global_managers.FCMManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.auth_flow.screens.select_profile.SelectProfileNav
import tv.dustypig.dustypig.ui.auth_flow.screens.sign_up.SignUpNav
import javax.inject.Inject


@HiltViewModel
class SignInViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val authManager: AuthManager,
    private val authRepository: AuthRepository,
    private val settingsManager: SettingsManager
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        SignInUIState(
            onSignIn = ::signIn,
            onHideError = ::hideError,
            onHideForgotPasswordError = ::hideForgotPasswordError,
            onHideForgotPasswordSuccess = ::hideForgotPasswordSuccess,
            onNavToSignUp = ::navToSignUp,
            onSendForgotPasswordEmail = ::sendForgotPasswordEmail
        )
    )
    val uiState: StateFlow<SignInUIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.lastLoginEmailFlow.collectLatest { email ->
                _uiState.update {
                    it.copy(
                        emailAddress = email
                    )
                }
            }
        }
    }

    private fun hideError() {
        _uiState.update {
            it.copy(showError = false)
        }
    }


    private fun hideForgotPasswordSuccess() {
        _uiState.update {
            it.copy(showForgotPasswordSuccess = false)
        }
    }

    private fun hideForgotPasswordError() {
        _uiState.update {
            it.copy(
                showForgotPasswordError = false
            )
        }
    }


    private fun signIn(email: String, password: String) {

        _uiState.update {
            it.copy(busy = true)
        }

        val fixedEmail = email.trim().lowercase()

        viewModelScope.launch {
            try {
                settingsManager.setLastLoginEmail(fixedEmail)

                val data = authRepository.passwordLogin(
                    PasswordCredentials(
                        fixedEmail,
                        password,
                        null,
                        settingsManager.getDeviceId()
                    )
                )
                if (data.loginType == LoginTypes.Account) {
                    authManager.setTempAuthToken(data.accountToken!!)
                    navigateToRoute(SelectProfileNav.route)
                    _uiState.update {
                        it.copy(busy = false)
                    }
                } else {

                    var profileToken = data.profileToken!!
                    if (settingsManager.getAllowNotifications(data.profileId!!)) {
                        authManager.setTempAuthToken(profileToken)
                        profileToken =
                            authRepository.updateFCMToken(FCMToken(FCMManager.currentToken))
                    }

                    authManager.login(
                        token = profileToken,
                        profileId = data.profileId,
                        isMain = data.loginType == LoginTypes.MainProfile
                    )
                }

            } catch (ex: Exception) {
                ex.logToCrashlytics()
                _uiState.update {
                    it.copy(
                        busy = false,
                        showError = true,
                        errorMessage = ex.localizedMessage
                    )
                }
            }
        }
    }


    private fun sendForgotPasswordEmail(email: String) {

        _uiState.update {
            it.copy(
                forgotPasswordBusy = true
            )
        }

        val fixedEmail = email.trim().lowercase()

        viewModelScope.launch {
            settingsManager.setLastLoginEmail(fixedEmail)

            try {
                authRepository.sendPasswordResetEmail(fixedEmail)
                _uiState.update {
                    it.copy(
                        forgotPasswordBusy = false,
                        //showForgotPassword = false,
                        showForgotPasswordSuccess = true
                    )
                }
            } catch (ex: Exception) {
                ex.logToCrashlytics()
                _uiState.update {
                    it.copy(
                        forgotPasswordBusy = false,
                        //showForgotPassword = false,
                        showForgotPasswordError = true,
                        errorMessage = ex.localizedMessage
                    )
                }
            }
        }
    }

    private fun navToSignUp(email: String) {
        viewModelScope.launch {
            settingsManager.setLastLoginEmail(email)
            navigateToRoute(SignUpNav.route)
        }
    }
}
