package tv.dustypig.dustypig.ui.auth_flow.screens.sign_up

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.models.LoginTypes
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.repositories.AccountRepository
import tv.dustypig.dustypig.api.repositories.AuthRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.global_managers.fcm_manager.FCMManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.auth_flow.SharedEmailModel
import tv.dustypig.dustypig.ui.auth_flow.screens.select_profile.SelectProfileNav
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val accountRepository: AccountRepository,
    private val authManager: AuthManager,
    private val authRepository: AuthRepository
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        SignUpUIState(
            email = SharedEmailModel.uiState.value.email,
            onHideError = ::hideError,
            onSignUp = ::signUp,
            onNavToSignIn = ::navToSignIn
        )
    )
    val uiState: StateFlow<SignUpUIState> = _uiState.asStateFlow()

    private fun hideError() {
        _uiState.update {
            it.copy(showError = false)
        }
    }

    private fun navToSignIn(email: String) {
        SharedEmailModel.updateEmail(email)
        popBackStack()
    }

    private fun signUp(name: String, email: String, password: String) {

        _uiState.update {
            it.copy(
                busy = true,
                email = email
            )
        }

        SharedEmailModel.updateEmail(email)

        viewModelScope.launch {
            try {
                val data = accountRepository.create(CreateAccount(email, password, name, null, FCMManager.currentToken))

                if(data.emailVerificationRequired == true) {
                    _uiState.update { it.copy(busy = false, showSuccess = true, message = "Please check your email to complete sign up") }
                } else {

                    //Email has been verified before, try to sign in
                    try{
                        val data2 = authRepository.passwordLogin(PasswordCredentials(email, password, FCMManager.currentToken))
                        if (data2.loginType == LoginTypes.Account) {
                            authManager.setTempAuthToken(data2.token!!)
                            navigateToRoute(SelectProfileNav.route)
                            _uiState.update {
                                it.copy(busy = false)
                            }
                        } else {
                            authManager.setAuthState(
                                token = data2.token!!,
                                profileId = data2.profileId!!,
                                isMain = data2.loginType == LoginTypes.MainProfile
                            )
                        }
                    } catch (ex: Exception) {

                        ex.logToCrashlytics()
                        //Login failed (wrong password), so just inform that they can sign in and go to the login screen
                        _uiState.update {
                            it.copy(
                                busy = false,
                                showSuccess = true,
                                message = "You may now sign in")
                        }
                    }
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
}
