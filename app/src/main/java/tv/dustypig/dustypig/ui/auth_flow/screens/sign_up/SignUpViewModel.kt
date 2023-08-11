package tv.dustypig.dustypig.ui.auth_flow.screens.sign_up

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
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.models.LoginResponse
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.throwIfError
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.auth_flow.SharedEmailModel
import tv.dustypig.dustypig.ui.auth_flow.screens.select_profile.SelectProfileScreenRoute
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator, application: Application
): AndroidViewModel(application), RouteNavigator by routeNavigator {

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
                val response = ThePig.api.createAccount(CreateAccount(uiState.value.email, uiState.value.password, uiState.value.name, null, null))
                response.throwIfError()
                val data = response.body()!!.data

                if(data.email_verification_required == true) {
                    _uiState.update { it.copy(busy = false, showSuccess = true, message = "Please check your email to complete sign up") }
                } else {

                    //Email has been verified before, try to sign in
                    try{
                        val response2 = ThePig.api.passwordLogin(PasswordCredentials(uiState.value.email, uiState.value.password, null))
                        response2.throwIfError()
                        val data2 = response2.body()!!.data

                        if (data2.login_type == LoginResponse.LOGIN_TYPE_ACCOUNT) {
                            AuthManager.setTempAuthToken(data2.token!!)
                            _uiState.update { it.copy(busy = false) }
                            navigateToRoute(SelectProfileScreenRoute.route)
                        } else {
                            AuthManager.setAuthState(getApplication<Application>().baseContext, data2.token!!, data2.profile_id!!, data2.login_type == LoginResponse.LOGIN_TYPE_MAIN_PROFILE)
                        }
                    } catch (_: Exception) {

                        //Login failed (wrong password), so just inform that they can sign in and go to the login screen
                        _uiState.update { it.copy(busy = false, showSuccess = true, message = "You may now sign in") }
                    }
                }
            } catch (ex: Exception) {
                _uiState.update { it.copy(busy = false, showError = true, message = ex.localizedMessage ?: "Unknown Error") }
            }
        }
    }
}
