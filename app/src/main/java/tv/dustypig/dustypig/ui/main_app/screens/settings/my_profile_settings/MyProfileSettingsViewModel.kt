package tv.dustypig.dustypig.ui.main_app.screens.settings.my_profile_settings

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import tv.dustypig.dustypig.api.models.DetailedProfile
import tv.dustypig.dustypig.api.models.UpdateProfile
import tv.dustypig.dustypig.api.repositories.ProfilesRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import java.io.ByteArrayOutputStream
import java.io.File
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
                    locked = _detailedProfile.locked,
                    avatarUrl = _detailedProfile.avatarUrl,
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
                    clearPin = pin == null,
                    locked = _detailedProfile.locked,
                    avatarUrl = _detailedProfile.avatarUrl,
                    allowedRatings = _detailedProfile.allowedRatings,
                    titleRequestPermissions = _detailedProfile.titleRequestPermissions
                )

                profilesRepository.update(updateProfile)

                _detailedProfile = _detailedProfile.copy(
                    hasPin = pin != null
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

    fun setAvatar(filePath: String) {
        _uiState.update {
            it.copy(busy = true)
        }

        viewModelScope.launch {
            try {

                val maxSize = 1024 * 1024
                var data = File(filePath).readBytes()
                var smallEnough = data.size <= maxSize && data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte()
                if(!smallEnough) {
                    val bitmap = BitmapFactory.decodeFile(filePath)
                    var quality = 100
                    while (!smallEnough) {
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                        if (stream.size() <= maxSize) {
                            data = stream.toByteArray()
                            smallEnough = true
                        } else {
                            quality -= 5
                            if (quality <= 0)
                                throw Exception("Could not shrink image to less than 1 MB. Please choose a different image.")
                        }
                    }
                }

                val newUrl = profilesRepository.setAvatar(_detailedProfile.id, data.toRequestBody())
                _detailedProfile = _detailedProfile.copy(
                    avatarUrl = newUrl
                )

                _uiState.update {
                    it.copy(
                        busy = false,
                        name = _detailedProfile.name,
                        avatarUrl = _detailedProfile.avatarUrl
                    )
                }

            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

}















