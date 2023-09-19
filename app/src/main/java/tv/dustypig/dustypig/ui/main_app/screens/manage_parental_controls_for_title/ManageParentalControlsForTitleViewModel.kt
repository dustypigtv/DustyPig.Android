package tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title

import android.content.Context
import android.widget.Toast
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
import tv.dustypig.dustypig.api.models.ProfileTitleOverrideInfo
import tv.dustypig.dustypig.api.models.TitlePermissionInfo
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

    private val _uiState = MutableStateFlow(ManageParentalControlsForTitleUIState())
    val uiState: StateFlow<ManageParentalControlsForTitleUIState> = _uiState.asStateFlow()

    private val _mediaId: Int = savedStateHandle.getOrThrow(AddToPlaylistNav.KEY_ID)

    private val _origValues: MutableMap<Int, Boolean> = mutableMapOf()

    private lateinit var _data: TitlePermissionInfo


    init {
        _uiState.update {
            it.copy(loading = true)
        }

        viewModelScope.launch {
            try{
                _data = mediaRepository.getTitlePermissions(_mediaId)

                for(profile in _data.profiles) {
                    _origValues[profile.profileId] = profile.state == OverrideState.Allow
                }
                _uiState.update {
                    it.copy(
                        loading = false,
                        permissionInfo = _data
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
                loading = false,
                busy = false,
                showErrorDialog = true,
                criticalError = criticalError,
                errorMessage = ex.localizedMessage
            )
        }
    }

    fun hideError(critical: Boolean) {
        _uiState.update {
            it.copy(showErrorDialog = false)
        }
        if(critical) {
            popBackStack()
        }
    }

    fun togglePermission(profileId: Int) {
        val profile = _data.profiles.first { it.profileId == profileId }
        profile.state = if(profile.state == OverrideState.Allow) OverrideState.Block else OverrideState.Allow

        var pendingChanges = false
        for(p in _data.profiles) {
            if(_origValues[p.profileId] != (p.state == OverrideState.Allow)) {
                pendingChanges = true
                break
            }
        }
        _uiState.update {
            it.copy(pendingChanges = pendingChanges)
        }
    }

    fun saveChanges(context: Context) {
        _uiState.update {
            it.copy(busy = true)
        }

        viewModelScope.launch {
            try{
                val profiles = ArrayList<ProfileTitleOverrideInfo>()
                for(p in _data.profiles) {
                    if(_origValues[p.profileId] != (p.state == OverrideState.Allow)) {
                        profiles.add(p)
                    }
                }
                if(profiles.isNotEmpty()) {
                    val tpi = TitlePermissionInfo(
                        titleId = _mediaId,
                        profiles = profiles
                    )
                    mediaRepository.setTitlePermissions(tpi)

                    for(p in _data.profiles) {
                        _origValues[p.profileId] = (p.state == OverrideState.Allow)
                    }

                    _uiState.update {
                        it.copy(
                            busy = false,
                            pendingChanges = false
                        )
                    }

                    Toast.makeText(context, "Changes Saved", Toast.LENGTH_SHORT).show()
                }
            } catch (ex: Exception){
                setError(ex = ex, criticalError = false)
            }
        }
    }





















}