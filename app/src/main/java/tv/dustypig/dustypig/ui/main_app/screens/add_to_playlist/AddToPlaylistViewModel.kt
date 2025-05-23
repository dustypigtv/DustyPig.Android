package tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.AddPlaylistItem
import tv.dustypig.dustypig.api.models.AddSeriesToPlaylistInfo
import tv.dustypig.dustypig.api.models.BasicPlaylist
import tv.dustypig.dustypig.api.models.CreatePlaylist
import tv.dustypig.dustypig.api.repositories.PlaylistRepository
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import javax.inject.Inject


@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val playlistRepository: PlaylistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), RouteNavigator by routeNavigator {

    private val TAG = "AddToPlaylistViewModel"

    private val _uiState = MutableStateFlow(
        AddToPlaylistUIState(
            onPopBackStack = ::popBackStack,
            onHideError = ::hideError,
            onNewPlaylist = ::newPlaylist,
            onSelectPlaylist = ::selectPlaylist
        )
    )
    val uiState = _uiState.asStateFlow()

    private var _existingPlaylists: List<BasicPlaylist> = listOf()

    private val _mediaId: Int = savedStateHandle.getOrThrow(AddToPlaylistNav.KEY_ID)
    private val _isSeries: Boolean = savedStateHandle.getOrThrow(AddToPlaylistNav.KEY_IS_SERIES)

    init {
        viewModelScope.launch {
            try {
                _existingPlaylists = playlistRepository.list()
                _uiState.update {
                    it.copy(
                        busy = false,
                        playlists = _existingPlaylists,
                        addingSeries = _isSeries
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
                errorMessage = ex.message,
                criticalError = criticalError
            )
        }
    }

    private fun hideError() {
        if (_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(
                    showErrorDialog = false
                )
            }
        }
    }

    private fun newPlaylist(name: String, autoAddEpisodes: Boolean) {
        Log.d(TAG, "newPlaylist: $name")
        _uiState.update {
            it.copy(
                busy = true
            )
        }

        viewModelScope.launch {
            try {
                val newId = playlistRepository.create(CreatePlaylist(name))
                Log.d(TAG, "newPlaylist: $newId")
                if (_isSeries) {
                    playlistRepository.addSeries(
                        AddSeriesToPlaylistInfo(
                            playlistId = newId,
                            mediaId = _mediaId,
                            autoAddNewEpisodes = autoAddEpisodes
                        )
                    )
                } else {
                    playlistRepository.addItem(AddPlaylistItem(newId, _mediaId))
                }

                HomeViewModel.triggerUpdate()
                popBackStack()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun selectPlaylist(id: Int, autoAddEpisodes: Boolean) {

        Log.d(TAG, "selectPlaylist: $id")

        _uiState.update {
            it.copy(
                busy = true
            )
        }

        viewModelScope.launch {
            try {
                if (_isSeries) {
                    playlistRepository.addSeries(
                        AddSeriesToPlaylistInfo(
                            playlistId = id,
                            mediaId = _mediaId,
                            autoAddNewEpisodes = autoAddEpisodes
                        )
                    )
                } else {
                    playlistRepository.addItem(
                        AddPlaylistItem(playlistId = id, mediaId = _mediaId)
                    )
                }

                HomeViewModel.triggerUpdate()
                popBackStack()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }
}





























