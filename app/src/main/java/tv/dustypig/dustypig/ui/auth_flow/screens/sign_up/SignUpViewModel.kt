package tv.dustypig.dustypig.ui.auth_flow.screens.sign_up

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.models.FCMToken
import tv.dustypig.dustypig.api.models.LoginTypes
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.repositories.AccountRepository
import tv.dustypig.dustypig.api.repositories.AuthRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.global_managers.FCMManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.auth_flow.screens.select_profile.SelectProfileNav
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val accountRepository: AccountRepository,
    private val authManager: AuthManager,
    private val authRepository: AuthRepository,
    private val settingsManager: SettingsManager
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        SignUpUIState(
            onHideError = ::hideError,
            onSignUp = ::signUp,
            onNavToSignIn = ::navToSignIn
        )
    )
    val uiState: StateFlow<SignUpUIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.lastLoginEmailFlow.collectLatest { email ->
                _uiState.update {
                    it.copy(email = email)
                }
            }
        }
    }

    private fun hideError() {
        _uiState.update {
            it.copy(showError = false)
        }
    }

    private fun navToSignIn(email: String) {
        viewModelScope.launch {
            settingsManager.setLastLoginEmail(email)
            popBackStack()
        }
    }

    private fun signUp(name: String, email: String, password: String) {

        _uiState.update {
            it.copy(
                busy = true,
                email = email
            )
        }

        viewModelScope.launch {

            settingsManager.setLastLoginEmail(email)

            try {
                accountRepository.create(
                    CreateAccount(email, password, name)
                )

                //Success - login
                try {
                    val data2 = authRepository.passwordLogin(
                        PasswordCredentials(
                            email,
                            password,
                            null,
                            settingsManager.getDeviceId()
                        )
                    )
                    if (data2.loginType == LoginTypes.Account) {
                        authManager.setTempAuthToken(data2.profileToken!!)
                        navigateToRoute(SelectProfileNav.route)
                        _uiState.update {
                            it.copy(busy = false)
                        }
                    } else {

                        var profileToken = data2.profileToken!!
                        if (settingsManager.getAllowNotifications(data2.profileId!!)) {
                            authManager.setTempAuthToken(profileToken)
                            profileToken =
                                authRepository.updateFCMToken(FCMToken(FCMManager.currentToken))
                        }

                        authManager.login(
                            token = profileToken,
                            profileId = data2.profileId,
                            isMain = data2.loginType == LoginTypes.MainProfile
                        )
                    }
                } catch (ex: Exception) {

                    ex.logToCrashlytics()
                    //Login failed (for reason other than wrong password)
                    _uiState.update {
                        it.copy(
                            busy = false,
                            showError = true,
                            errorMessage = ex.localizedMessage
                        )
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
