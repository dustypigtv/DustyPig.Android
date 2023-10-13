package tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.repositories.ProfilesRepository
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile.EditProfileNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile.EditProfileViewModel
import javax.inject.Inject

@HiltViewModel
class ProfilesSettingsViewModel @Inject constructor(
    private val profilesRepository: ProfilesRepository,
    routeNavigator: RouteNavigator
): ViewModel(), RouteNavigator by routeNavigator {

    companion object {
        const val TAG = "ProfileSettingsVM"

        private val _needsUpdate = MutableStateFlow(false)

        fun triggerUpdate() {
            _needsUpdate.tryEmit(true)
        }
    }

    private val _uiState = MutableStateFlow((ProfilesSettingsUIState()))
    val uiState = _uiState.asStateFlow()

    init {

        //This automatically updates data on init, so set this to false to prevent 2 calls
        _needsUpdate.update { false }

        //Do the update
        viewModelScope.launch {
            updateData(true)
        }

        //Now start watching for updates from other screens
        viewModelScope.launch {
            _needsUpdate.collectLatest { needsUpdate ->
                if(needsUpdate)
                    updateData(false)
            }
        }
    }

    private suspend fun updateData(setCriticalError: Boolean) {
        try {
            _uiState.update {
                it.copy(busy = true)
            }

            val lst = profilesRepository.list()

            _uiState.update {
                it.copy(
                    busy = false,
                    profiles = lst
                )
            }
        } catch (ex: Exception) {
            setError(ex = ex, criticalError = setCriticalError)
        }
    }

    private fun setError(ex: Exception, criticalError: Boolean) {
        ex.printStackTrace()
        Log.d(TAG, ex.localizedMessage, ex)
        _uiState.update {
            it.copy(
                busy = false,
                showErrorDialog = true,
                criticalError = criticalError
            )
        }
    }

    fun hideError() {
        if(_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }

    fun navToAddProfile() {
        val color = listOf("blue", "gold", "green", "grey", "red").random()
        EditProfileViewModel.preloadAvatar = "https://s3.dustypig.tv/user-art/profile/${color}.png"
        EditProfileViewModel.selectedProfileId = 0
        navigateToRoute(EditProfileNav.route)
    }


    fun navToProfile(id: Int) {
        EditProfileViewModel.preloadAvatar = _uiState.value.profiles.first { it.id == id }.avatarUrl ?: ""
        EditProfileViewModel.selectedProfileId = id
        navigateToRoute(EditProfileNav.route)
    }

}