package tv.dustypig.dustypig.ui.main_app.screens.playlist_details

//import tv.dustypig.dustypig.download_manager.DownloadManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.PlaylistItem
import tv.dustypig.dustypig.api.models.UpdatesPlaylist
import tv.dustypig.dustypig.api.repositories.PlaylistRepository
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val playlistRepository: PlaylistRepository,
    private val downloadManager: DownloadManager,
    savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(PlaylistDetailsUIState(downloadManager = downloadManager))
    val uiState = _uiState.asStateFlow()


    private val _playlistId: Int = savedStateHandle.getOrThrow(PlaylistDetailsNav.KEY_ID)
    private lateinit var _detailedPlaylist: DetailedPlaylist
    private val _localItems = mutableListOf<PlaylistItem>()

    init {

        _uiState.update {
            it.copy(
                loading = true,
                posterUrl = ScreenLoadingInfo.posterUrl,
                title = ScreenLoadingInfo.title
            )
        }

        viewModelScope.launch {
            try{
                _detailedPlaylist = playlistRepository.details(_playlistId)
                val items = _detailedPlaylist.items ?: listOf()
                _localItems.addAll(items)
                val upNext = items.firstOrNull { it.index == _detailedPlaylist.currentIndex } ?: items.firstOrNull()
                var partiallyPlayed = false
                if(upNext != null) {
                    if(upNext.mediaType == MediaTypes.Movie) {
                        partiallyPlayed = (upNext.played ?: 0.0) > 1.0 && (upNext.played ?: 0.0) < (upNext.creditStartTime ?: (upNext.length - 30))
                    } else if(upNext.mediaType == MediaTypes.Episode) {
                        partiallyPlayed = (upNext.played ?: 0.0) > 1.0 && (upNext.played ?: 0.0) < (upNext.creditStartTime ?: (upNext.length * 0.9))
                    }
                }

                _uiState.update {
                    it.copy(
                        playlistId = _playlistId,
                        loading = false,
                        title = _detailedPlaylist.name,
                        canPlay = items.isNotEmpty(),
                        upNextTitle = upNext?.title ?: "",
                        partiallyPlayed = partiallyPlayed,
                        items = items,
                        updateList = true
                    )
                }
            } catch(ex: Exception) {
                setError(ex = ex, criticalError = true)
            }
        }
    }

    private fun setError(ex: Exception, criticalError: Boolean) {
        _uiState.update {
            it.copy(
                loading = false,
                busy = false,
                showErrorDialog = true,
                errorMessage = ex.localizedMessage,
                criticalError = criticalError
            )
        }
    }

    fun hideError() {
        if(_uiState.value.criticalError) {
            popBackStack()
        }
        else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }

    fun showRenameDialog() {
        _uiState.update {
            it.copy(showRenameDialog = true)
        }
    }

    fun hideRenameDialog(confirmed: Boolean, newName: String = "") {
        if(confirmed) {
            _uiState.update {
                it.copy(
                    showRenameDialog = false,
                    busy = true
                )
            }
            viewModelScope.launch {
                try {
                    playlistRepository.rename(UpdatesPlaylist(
                        id = _playlistId,
                        name = newName
                    ))
                    _uiState.update {
                        it.copy(
                            busy = false,
                            title = newName
                        )
                    }
                } catch (ex: Exception) {
                    setError(ex = ex, criticalError = false)
                }
            }
        } else {
            _uiState.update {
                it.copy(showRenameDialog = false)
            }
        }
    }

    fun showDownloadDialog() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showDownloadDialog = true,
                    currentDownloadCount = downloadManager.getJobCount(_playlistId, MediaTypes.Playlist)
                )
            }
        }
    }

    fun hideDownloadDialog(newCount: Int) {
        _uiState.update {
            it.copy(showDownloadDialog = false)
        }
        viewModelScope.launch {
            if (newCount == 0)
                downloadManager.delete(_playlistId, MediaTypes.Playlist)
            else
                downloadManager.addOrUpdatePlaylist(_detailedPlaylist, newCount)
        }
    }

    fun listUpdated() {
        _uiState.update {
            it.copy(updateList = false)
        }
    }

    fun updateListOrderOnServer(from: Int, to: Int) {

        _uiState.update {
            it.copy(busy = true)
        }

        viewModelScope.launch {
            try {
                playlistRepository.moveItem(_detailedPlaylist.items!![from].id, to)
                _detailedPlaylist.items = _detailedPlaylist.items!!.toMutableList().apply {
                    add(to, removeAt(from))
                }
                _uiState.update {
                    it.copy(busy = false)
                }
            } catch (ex: Exception) {
                _localItems.clear()
                _localItems.addAll(_detailedPlaylist.items ?: listOf())
                _uiState.update {
                    it.copy(
                        showErrorDialog = true,
                        errorMessage = ex.localizedMessage,
                        items = _localItems,
                        updateList = true,
                        busy = false
                    )
                }
            }
        }
    }

    fun deleteItem(id: Int) {
        _localItems.remove(_localItems.first{it.id == id})
        _uiState.update {
            it.copy(
                updateList = true,
                items = _localItems,
                busy = true
            )
        }
        viewModelScope.launch {
            try {
                playlistRepository.deleteItem(id)
                _detailedPlaylist.items = _detailedPlaylist.items!!.toMutableList().apply {
                    remove(_detailedPlaylist.items!!.first{it.id == id})
                }
                _uiState.update {
                    it.copy(busy = false)
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    fun deletePlaylist() {
        _uiState.update {
            it.copy(showDeleteDialog = true)
        }
    }

    fun hideDeletePlaylistDialog(confirmed: Boolean) {
        if(confirmed) {
            _uiState.update {
                it.copy(
                    showDeleteDialog = false,
                    busy = true
                )
            }
            viewModelScope.launch {
                try {
                    playlistRepository.deletePlaylist(_detailedPlaylist.id)
                    //DownloadManager.delete(_detailedPlaylist.id)
                    HomeViewModel.triggerUpdate()
                    popBackStack()
                } catch (ex: Exception) {
                    setError(ex = ex, criticalError = false)
                }
            }
        } else {
            _uiState.update {
                it.copy(showDeleteDialog = false)
            }
        }
    }

    fun playUpNext() {
        val upNext = _detailedPlaylist.items!!.firstOrNull { it.index == _detailedPlaylist.currentIndex } ?: _detailedPlaylist.items!!.first()
        navigateToRoute(PlayerNav.getRouteForId(upNext.id))
    }

    fun playItem(id: Int) {
        navigateToRoute(PlayerNav.getRouteForId(id))
    }

    fun navToItem(id: Int) {
        val pli = _detailedPlaylist.items?.firstOrNull { it.id == id } ?: return
        if(pli.mediaType == MediaTypes.Movie) {
            navigateToRoute(MovieDetailsNav.getRouteForId(pli.mediaId))
        } else if(pli.mediaType == MediaTypes.Episode) {
            navigateToRoute(EpisodeDetailsNav.getRoute(id = pli.mediaId, canPlay = true, fromSeriesDetails = false))
        }
    }
}