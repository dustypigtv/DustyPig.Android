package tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.repositories.AccountRepository
import tv.dustypig.dustypig.api.repositories.AuthRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val authManager: AuthManager,
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(AccountSettingsUIState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                busy = false,
                isMainProfile = authManager.currentProfileIsMain
            )
        }
    }

    private fun setError(ex: Exception) {
        _uiState.update {
            it.copy(
                busy = false,
                showErrorDialog = true,
                errorMessage = ex.localizedMessage,
                loginToDeviceSuccess = false,
                showChangePasswordSuccessAlert = false
            )
        }
    }

    fun hideError() {
        _uiState.update {
            it.copy(showErrorDialog = false)
        }
    }

    fun loginToDevice(code: String) {
       viewModelScope.launch {
            try{
                authRepository.loginDeviceWithCode(code)
                _uiState.update {
                    it.copy(busy = true)
                }
            } catch (ex: Exception) {
                setError(ex)
            }
        }
    }


    fun changePassword(newPassword: String) {
        _uiState.update {
            it.copy(busy = true)
        }
        viewModelScope.launch{
            try {
                accountRepository.changePassword(newPassword)
                _uiState.update {
                    it.copy(
                        busy = false,
                        showChangePasswordSuccessAlert = true
                    )
                }
            } catch (ex: Exception) {
                setError(ex)
            }
        }
    }

    fun hideChangePasswordDialogs() {
        _uiState.update {
            it.copy(
                showChangePasswordSuccessAlert = false
            )
        }
    }

    fun signOut() {
        _uiState.update {
            it.copy(busy = true)
        }
        viewModelScope.launch {
            authRepository.signout()
            authManager.logout()
        }
    }

    fun signOutEverywhere() {
        _uiState.update {
            it.copy(busy = true)
        }
        viewModelScope.launch {
            try {
                authRepository.signoutEverywhere()
                authManager.logout()
            } catch (ex: Exception) {
                setError(ex)
            }
        }
    }


    fun deleteAccount() {
        _uiState.update {
            it.copy(busy = true)
        }
        viewModelScope.launch {
            try {
                accountRepository.delete()
                authManager.logout()
            } catch (ex: Exception) {
                setError(ex)
            }
        }
    }
}