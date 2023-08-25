package tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.ThePig
import tv.dustypig.dustypig.api.models.AddPlaylistItem
import tv.dustypig.dustypig.api.models.BasicPlaylist
import tv.dustypig.dustypig.api.models.CreatePlaylist
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import javax.inject.Inject

@HiltViewModel
class AddToPlaylistViewModel  @Inject constructor(
    private val routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(AddToPlaylistUIState())
    val uiState: StateFlow<AddToPlaylistUIState> = _uiState.asStateFlow()

    private var _data: List<BasicPlaylist> = listOf()

    private val _mediaId: Int = savedStateHandle.getOrThrow(AddToPlaylistNav.KEY_ID)
    private val _isSeries: Boolean = savedStateHandle.getOrThrow<String>(AddToPlaylistNav.KEY_IS_SERIES).toBoolean()

    init {
        _uiState.update {
            it.copy(
                busy = true
            )
        }


        viewModelScope.launch {
            try {
                _data = ThePig.Api.Playlists.listPlaylists()
                _uiState.update {
                    it.copy(
                        busy = false,
                        playlists = _data
                    )
                }
            } catch(ex: Exception) {
                _uiState.update {
                    it.copy(
                        busy = false,
                        showError = true,
                        criticalError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }

    fun hideError(critical: Boolean) {
        _uiState.update {
            it.copy(
                showError = false
            )
        }
        if(critical) {
            popBackStack()
        }
    }

    fun newPlaylist(name: String) {
        _uiState.update {
            it.copy(
                busy = true
            )
        }

        viewModelScope.launch {
            try{
                val newId = ThePig.Api.Playlists.createPlaylist(CreatePlaylist(name))
                if(_isSeries) {
                    ThePig.Api.Playlists.addSeriesToPlaylist(AddPlaylistItem(newId, _mediaId))
                }
                else {
                    ThePig.Api.Playlists.addItemToPlaylist(AddPlaylistItem(newId, _mediaId))
                }
                popBackStack()
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        busy = false,
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }

    fun selectPlaylist(id: Int) {
        _uiState.update {
            it.copy(
                busy = true
            )
        }

        viewModelScope.launch {
            try{
                if(_isSeries) {
                    ThePig.Api.Playlists.addSeriesToPlaylist(AddPlaylistItem(id, _mediaId))
                }
                else {
                    ThePig.Api.Playlists.addItemToPlaylist(AddPlaylistItem(id, _mediaId))
                }
                popBackStack()
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        busy = false,
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }
}





























