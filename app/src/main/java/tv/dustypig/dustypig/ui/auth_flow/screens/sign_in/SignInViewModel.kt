package tv.dustypig.dustypig.ui.auth_flow.screens.sign_in

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.LoginTypes
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.repositories.AuthRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.global_managers.fcm_manager.FCMManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.auth_flow.SharedEmailModel
import tv.dustypig.dustypig.ui.auth_flow.screens.select_profile.SelectProfileNav
import tv.dustypig.dustypig.ui.auth_flow.screens.sign_up.SignUpNav
import javax.inject.Inject


@HiltViewModel
class SignInViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val authManager: AuthManager,
    private val authRepository: AuthRepository
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(SignInUIState())
    val uiState: StateFlow<SignInUIState> = _uiState.asStateFlow()

    fun hideError() {
        _uiState.update {
            it.copy(showError = false)
        }
    }

    fun showForgotPassword(email: String) {
        SharedEmailModel.updateEmail(email)
        _uiState.update {
            it.copy(showForgotPassword = true)
        }
    }

    fun hideForgotPassword(email: String) {
        SharedEmailModel.updateEmail(email)
        _uiState.update {
            it.copy(
                showForgotPassword = false
            )
        }
    }

    fun hideForgotPasswordSuccess() {
        _uiState.update {
            it.copy(showForgotPasswordSuccess = false)
        }
    }

    fun hideForgotPasswordError() {
        _uiState.update {
            it.copy(
                showForgotPassword = true,
                showForgotPasswordError = false
            )
        }
    }


    fun signIn(email: String, password: String){

        _uiState.update {
            it.copy(busy = true)
        }

        val fixedEmail = email.trim().lowercase()
        SharedEmailModel.updateEmail(fixedEmail)

        viewModelScope.launch {
            try {

                val data = authRepository.passwordLogin(
                    PasswordCredentials(
                        fixedEmail,
                        password,
                        FCMManager.currentToken
                    )
                )
                if (data.loginType == LoginTypes.Account) {
                    authManager.setTempAuthToken(data.token!!)
                    navigateToRoute(SelectProfileNav.route)
                    _uiState.update {
                        it.copy(busy = false)
                    }
                } else {
                    authManager.setAuthState(
                        token = data.token!!,
                        profileId = data.profileId!!,
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


    fun sendForgotPasswordEmail(email: String) {

        _uiState.update {
            it.copy(
                forgotPasswordBusy = true
            )
        }

        val fixedEmail = email.trim().lowercase()
        SharedEmailModel.updateEmail(fixedEmail)

        viewModelScope.launch {
            try {
                authRepository.sendPasswordResetEmail(fixedEmail)
                _uiState.update {
                    it.copy(
                        forgotPasswordBusy = false,
                        showForgotPassword = false,
                        showForgotPasswordSuccess = true
                    )
                }
            } catch (ex: Exception) {
                ex.logToCrashlytics()
                _uiState.update {
                    it.copy(
                        forgotPasswordBusy = false,
                        showForgotPassword = false,
                        showForgotPasswordError = true,
                        errorMessage = ex.localizedMessage
                    )
                }
            }
        }
    }

    fun navToSignUp(email: String) {
        SharedEmailModel.updateEmail(email)
        navigateToRoute(SignUpNav.route)
    }
}
