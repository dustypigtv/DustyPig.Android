package tv.dustypig.dustypig.ui.main_app.screens.settings.my_profile_settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.DetailedProfile
import tv.dustypig.dustypig.api.models.UpdateProfile
import tv.dustypig.dustypig.api.repositories.ProfilesRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class MyProfileSettingsViewModel @Inject constructor(
    private val profilesRepository: ProfilesRepository,
    private val authManager: AuthManager,
    routeNavigator: RouteNavigator
): ViewModel(), RouteNavigator by routeNavigator {

    private val TAG = "MyProfileSettingsVM"

    private val _uiState = MutableStateFlow((MyProfileSettingsUIState()))
    val uiState = _uiState.asStateFlow()

    private lateinit var _detailedProfile: DetailedProfile

    init {
        viewModelScope.launch {
            try {
                _detailedProfile = profilesRepository.details(authManager.currentProfileId)
                _uiState.update {
                    it.copy(
                        busy = false,
                        name = _detailedProfile.name,
                        avatarUrl = _detailedProfile.avatarUrl
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = true)
            }
        }
    }

    private fun setError(ex: Exception, criticalError: Boolean) {
        Log.e(TAG, ex.localizedMessage, ex)
        if(criticalError)
            ex.logToCrashlytics()

        _uiState.update {
            it.copy(
                busy = false,
                showError = true,
                errorMessage = ex.localizedMessage
            )
        }
    }

    fun hideError() {
        _uiState.update {
            it.copy(showError = false)
        }
    }


    fun renameProfile(newName: String) {
        _uiState.update {
            it.copy(busy = true)
        }

        viewModelScope.launch {
            try {
                val updateProfile = UpdateProfile(
                    id = _detailedProfile.id,
                    name = newName.trim(),
                    pin = _detailedProfile.pin,
                    locked = _detailedProfile.locked,
                    avatarImage = null, // server ignores this field if null
                    allowedRatings = _detailedProfile.allowedRatings,
                    titleRequestPermissions = _detailedProfile.titleRequestPermissions
                )

                profilesRepository.update(updateProfile)

                _detailedProfile = _detailedProfile.copy(
                    name = updateProfile.name
                )

                _uiState.update {
                    it.copy(
                        busy = false,
                        name = updateProfile.name
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    fun setPinNumber(newPin: String) {
        _uiState.update {
            it.copy(busy = true)
        }

        viewModelScope.launch {
            try {

                val pin: UShort? =
                    if(newPin.isEmpty())
                        null
                    else
                        newPin.toUShort()

                val updateProfile = UpdateProfile(
                    id = _detailedProfile.id,
                    name = _detailedProfile.name,
                    pin = pin,
                    locked = _detailedProfile.locked,
                    avatarImage = null, // server ignores this field if null
                    allowedRatings = _detailedProfile.allowedRatings,
                    titleRequestPermissions = _detailedProfile.titleRequestPermissions
                )

                profilesRepository.update(updateProfile)

                _detailedProfile = _detailedProfile.copy(
                    pin = pin
                )

                _uiState.update {
                    it.copy(
                        busy = false
                    )
                }

            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }


}