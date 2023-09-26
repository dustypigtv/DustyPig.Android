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
                showLoginToDeviceDialog = false,
                showLoginToDeviceAlert = false,
                loginToDeviceSuccess = false,
                showChangePasswordDialog = false,
                showChangePasswordSuccessAlert = false,
                showSignoutEverywhereDialog = false,
                showDeleteAccountDialog = false
            )
        }
    }

    fun hideErrorDialog() {
        _uiState.update {
            it.copy(showErrorDialog = false)
        }
    }





    fun showLoginToDeviceDialog() {
        _uiState.update {
            it.copy(showLoginToDeviceDialog = true)
        }
    }

    fun hideLoginToDeviceDialogs() {
        _uiState.update {
            it.copy(
                showLoginToDeviceDialog = false,
                showLoginToDeviceAlert = false
            )
        }
    }

    fun loginToDevice(code: String) {
        _uiState.update {
            it.copy(
                busy = true,
                showLoginToDeviceDialog = false
            )
        }
        viewModelScope.launch {
            try{
                authRepository.loginDeviceWithCode(code)
                _uiState.update {
                    it.copy(
                        busy = true,
                        showLoginToDeviceAlert = true
                    )
                }
            } catch (ex: Exception) {
                setError(ex)
            }
        }
    }






    fun showChangePasswordDialog() {
        _uiState.update {
            it.copy(showChangePasswordDialog = true)
        }
    }

    fun hideChangePasswordDialogs() {
        _uiState.update {
            it.copy(
                showChangePasswordDialog = false,
                showChangePasswordSuccessAlert = false
            )
        }
    }


    fun changePassword(newPassword: String) {
        _uiState.update {
            it.copy(
                busy = true,
                showChangePasswordDialog = false
            )
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


    fun signout() {
        _uiState.update {
            it.copy(busy = true)
        }
        viewModelScope.launch {
            authRepository.signout()
            authManager.logout()
        }
    }

    fun showSignoutEverywhereDialog() {
        _uiState.update {
            it.copy(showSignoutEverywhereDialog = true)
        }
    }

    fun hideSignoutEverywhereDialog(confirmed: Boolean) {
        if(confirmed) {
            _uiState.update {
                it.copy(
                    busy = true,
                    showSignoutEverywhereDialog = false
                )
            }
            viewModelScope.launch {
                try{
                    authRepository.signoutEverywhere()
                    authManager.logout()
                } catch(ex: Exception) {
                    setError(ex)
                }
            }
        }
        else {
            _uiState.update {
                it.copy(showSignoutEverywhereDialog = false)
            }
        }
    }


    fun showDeleteAccountDialog() {
        _uiState.update {
            it.copy(showDeleteAccountDialog = true)
        }
    }




    fun hideDeleteAccountDialog(confirmed: Boolean) {
        if(confirmed) {
            _uiState.update {
                it.copy(
                    busy = true,
                    showDeleteAccountDialog = false
                )
            }
            viewModelScope.launch {
                try{
                    accountRepository.delete()
                    authManager.logout()
                } catch (ex: Exception) {
                    setError(ex)
                }
            }
        } else {
            _uiState.update {
                it.copy(showDeleteAccountDialog = false)
            }
        }
    }



}