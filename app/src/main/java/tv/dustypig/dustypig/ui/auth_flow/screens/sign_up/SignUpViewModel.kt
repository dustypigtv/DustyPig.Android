package tv.dustypig.dustypig.ui.auth_flow.screens.sign_up

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.FCMManager
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.models.LoginTypes
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.repositories.AccountRepository
import tv.dustypig.dustypig.api.repositories.AuthRepository
import tv.dustypig.dustypig.global_managers.AuthManager
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

    private val _uiState = MutableStateFlow(SignUpUIState(email = SharedEmailModel.uiState.value.email))
    val uiState: StateFlow<SignUpUIState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name ) }
    }

    fun updateEmail(email: String) {
        SharedEmailModel.updateEmail(email)
        _uiState.update { it.copy(email = email.trim().lowercase()) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun hideError() {
        _uiState.update { it.copy(showError = false) }
    }

    fun navToSignIn() {
        popBackStack()
    }

    fun signUp() {
        _uiState.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                val data = accountRepository.create(CreateAccount(uiState.value.email, uiState.value.password, uiState.value.name, null, FCMManager.currentToken))

                if(data.emailVerificationRequired == true) {
                    _uiState.update { it.copy(busy = false, showSuccess = true, message = "Please check your email to complete sign up") }
                } else {

                    //Email has been verified before, try to sign in
                    try{
                        val data2 = authRepository.passwordLogin(PasswordCredentials(uiState.value.email, uiState.value.password, FCMManager.currentToken))
                        if (data2.loginType == LoginTypes.Account) {
                            authManager.setTempAuthToken(data2.token!!)
                            _uiState.update { it.copy(busy = false) }
                            navigateToRoute(SelectProfileNav.route)
                        } else {
                            authManager.setAuthState(data2.token!!, data2.profileId!!, data2.loginType == LoginTypes.MainProfile)
                        }
                    } catch (ex: Exception) {

                        ex.logToCrashlytics()
                        //Login failed (wrong password), so just inform that they can sign in and go to the login screen
                        _uiState.update { it.copy(busy = false, showSuccess = true, message = "You may now sign in") }
                    }
                }
            } catch (ex: Exception) {
                ex.logToCrashlytics()
                _uiState.update { it.copy(busy = false, showError = true, message = ex.localizedMessage ?: "Unknown Error") }
            }
        }
    }
}
