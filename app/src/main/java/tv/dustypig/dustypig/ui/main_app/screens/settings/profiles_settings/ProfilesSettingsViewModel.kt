package tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.repositories.ProfilesRepository
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class ProfilesSettingsViewModel @Inject constructor(
    private val profilesRepository: ProfilesRepository,
    routeNavigator: RouteNavigator
): ViewModel(), RouteNavigator by routeNavigator {

    private val TAG = "ProfileSettingsVM"

    private val _uiState = MutableStateFlow((ProfilesSettingsUIState()))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val lst = profilesRepository.list()
                _uiState.update {
                    it.copy(
                        busy = false,
                        profiles = lst
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = true)
            }
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

    }

    fun navToProfile(id: Int) {

    }

}