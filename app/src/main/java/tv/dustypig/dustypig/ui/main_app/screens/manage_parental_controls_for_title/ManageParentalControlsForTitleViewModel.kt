package tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.OverrideState
import tv.dustypig.dustypig.api.models.SetTitlePermission
import tv.dustypig.dustypig.api.models.TitlePermissions
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import javax.inject.Inject

@HiltViewModel
class ManageParentalControlsForTitleViewModel  @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val mediaRepository: MediaRepository,
    savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator{

    private val _uiState = MutableStateFlow(
        ManageParentalControlsForTitleUIState(
            onHideError = ::hideError,
            onPopBackStack = ::popBackStack,
            onTogglePermission = ::togglePermission
        )
    )
    val uiState: StateFlow<ManageParentalControlsForTitleUIState> = _uiState.asStateFlow()

    private val _mediaId: Int = savedStateHandle.getOrThrow(AddToPlaylistNav.KEY_ID)

    private val _origValues: MutableMap<Int, Boolean> = mutableMapOf()

    private lateinit var _data: TitlePermissions


    init {
        _uiState.update {
            it.copy(busy = true)
        }

        viewModelScope.launch {
            try{
                _data = mediaRepository.getTitlePermissions(_mediaId)

                for(profile in _data.subProfiles) {
                    _origValues[profile.profileId] = profile.overrideState == OverrideState.Allow
                }
                for(profile in _data.friendProfiles) {
                    _origValues[profile.profileId] = profile.overrideState == OverrideState.Allow
                }
                _uiState.update {
                    it.copy(
                        busy = false,
                        subProfiles = _data.subProfiles,
                        friendProfiles = _data.friendProfiles
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = true)
            }
        }
    }

    private fun setError(ex: Exception, criticalError: Boolean) {
        ex.logToCrashlytics()
        _uiState.update {
            it.copy(
                busy = false,
                showErrorDialog = true,
                criticalError = criticalError,
                errorMessage = ex.localizedMessage
            )
        }
    }

    private fun hideError() {
        if (_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }

    private fun togglePermission(profileId: Int) {

        _uiState.update {
            it.copy(busy = true)
        }

        viewModelScope.launch {
            try{
                var profile = _data.subProfiles.firstOrNull { it.profileId == profileId }
                if(profile == null)
                    profile = _data.friendProfiles.first { it.profileId == profileId }

                profile.overrideState = if(profile.overrideState == OverrideState.Allow) OverrideState.Block else OverrideState.Allow

                mediaRepository.setTitlePermissions(
                    setTitlePermissionInfo = SetTitlePermission(
                        mediaId = _mediaId,
                        profileId = profileId,
                        overrideState = profile.overrideState
                    )
                )

                _uiState.update {
                    it.copy(busy = false)
                }

            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }


        var pendingChanges = false
        for(p in _data.subProfiles) {
            if(_origValues[p.profileId] != (p.overrideState == OverrideState.Allow)) {
                pendingChanges = true
                break
            }
        }
        _uiState.update {
            it.copy(pendingChanges = pendingChanges)
        }
    }
}