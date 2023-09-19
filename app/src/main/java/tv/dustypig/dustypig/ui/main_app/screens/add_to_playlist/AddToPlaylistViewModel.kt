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
import tv.dustypig.dustypig.api.models.AddPlaylistItem
import tv.dustypig.dustypig.api.models.BasicPlaylist
import tv.dustypig.dustypig.api.models.CreatePlaylist
import tv.dustypig.dustypig.api.repositories.PlaylistRepository
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import javax.inject.Inject

@HiltViewModel
class AddToPlaylistViewModel  @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val playlistRepository: PlaylistRepository,
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
                loading = true
            )
        }

        viewModelScope.launch {
            try {
                _data = playlistRepository.list()
                _uiState.update {
                    it.copy(
                        loading = false,
                        playlists = _data
                    )
                }
            } catch(ex: Exception) {
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
                errorMessage = ex.message,
                criticalError = criticalError
            )
        }
    }

    fun hideError() {
        if(_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(
                    showErrorDialog = false
                )
            }
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
                val newId = playlistRepository.create(CreatePlaylist(name))
                if(_isSeries) {
                    playlistRepository.addSeries(AddPlaylistItem(newId, _mediaId))
                }
                else {
                    playlistRepository.addItem(AddPlaylistItem(newId, _mediaId))
                }

                HomeViewModel.triggerUpdate()
                popBackStack()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
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
                    playlistRepository.addSeries(AddPlaylistItem(id, _mediaId))
                }
                else {
                    playlistRepository.addItem(AddPlaylistItem(id, _mediaId))
                }

                HomeViewModel.triggerUpdate()
                popBackStack()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }
}





























