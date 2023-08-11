package tv.dustypig.dustypig.ui.auth_flow.screens.sign_in

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.AuthManager
import tv.dustypig.dustypig.api.ThePig
import tv.dustypig.dustypig.api.models.LoginResponse
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.models.SimpleValue
import tv.dustypig.dustypig.api.throwIfError
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.auth_flow.SharedEmailModel
import tv.dustypig.dustypig.ui.auth_flow.screens.select_profile.SelectProfileScreenRoute
import tv.dustypig.dustypig.ui.auth_flow.screens.sign_up.SignUpScreenRoute
import javax.inject.Inject


@HiltViewModel
class SignInViewModel  @Inject constructor(
    private val routeNavigator: RouteNavigator, application: Application
): AndroidViewModel(application), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(SignInUIState(email = SharedEmailModel.uiState.value.email))
    val uiState: StateFlow<SignInUIState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        SharedEmailModel.updateEmail(email)
        if(SharedEmailModel.uiState.value.email == AuthManager.TEST_USER){
            _uiState.update { it.copy(email = AuthManager.TEST_USER, password = AuthManager.TEST_PASSWORD) }
        } else {
            _uiState.update { it.copy(email = email.trim().lowercase()) }
        }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun hideError() {
        _uiState.update { it.copy(showError = false) }
    }

    fun showForgotPassword() {
        _uiState.update { it.copy(showForgotPassword = true) }
    }

    fun hideForgotPassword() {
        _uiState.update { it.copy(showForgotPassword = false) }
    }

    fun hideForgotPasswordSuccess() {
        _uiState.update { it.copy(showForgotPasswordSuccess = false) }
    }

    fun hideForgotPasswordError() {
        _uiState.update { it.copy(showForgotPassword = true, showForgotPasswordError = false) }
    }


    fun signIn(){
        _uiState.update { it.copy(busy = true) }

        viewModelScope.launch {
            try {

                val response = ThePig.api.passwordLogin(
                    PasswordCredentials(
                        uiState.value.email,
                        uiState.value.password,
                        null
                    )
                )
                response.throwIfError()

                val data = response.body()!!.data
                if (data.login_type == LoginResponse.LOGIN_TYPE_ACCOUNT) {
                    AuthManager.setTempAuthToken(data.token!!)
                    _uiState.update { it.copy(busy = false) }
                    navigateToRoute(SelectProfileScreenRoute.route)
                } else {
                    AuthManager.setAuthState(getApplication<Application>().baseContext, data.token!!, data.profile_id!!, data.login_type == LoginResponse.LOGIN_TYPE_MAIN_PROFILE)
                }

            } catch (ex: Exception) {
                _uiState.update { it.copy(busy = false, showError = true, errorMessage = ex.localizedMessage ?: "Unknown Error") }
            }
        }
    }


    fun sendForgotPasswordEmail() {
        _uiState.update { it.copy(forgotPasswordBusy = true) }
        viewModelScope.launch {
            try {
                val response = ThePig.api.sendPasswordResetEmail(SimpleValue(uiState.value.email))
                response.throwIfError()
                _uiState.update { it.copy(forgotPasswordBusy = false, showForgotPassword = false, showForgotPasswordSuccess = true) }
            } catch (ex: Exception) {
                _uiState.update { it.copy(forgotPasswordBusy = false, showForgotPassword = false, showForgotPasswordError = true, errorMessage = ex.localizedMessage ?: "Unknown Error") }
            }
        }
    }

    fun navToSignUp() {
       navigateToRoute(SignUpScreenRoute.route)
    }
}
