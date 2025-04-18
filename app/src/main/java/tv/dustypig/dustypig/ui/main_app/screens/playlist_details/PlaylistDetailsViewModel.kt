package tv.dustypig.dustypig.ui.main_app.screens.playlist_details

import androidx.annotation.OptIn
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.PlaylistItem
import tv.dustypig.dustypig.api.models.UpdatesPlaylist
import tv.dustypig.dustypig.api.repositories.PlaylistRepository
import tv.dustypig.dustypig.global_managers.ArtworkCache
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.download_manager.MyDownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.UIJob
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import javax.inject.Inject

@HiltViewModel
@OptIn(UnstableApi::class)
class PlaylistDetailsViewModel
@Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val playlistRepository: PlaylistRepository,
    private val downloadManager: MyDownloadManager,
    castManager: CastManager,
    savedStateHandle: SavedStateHandle
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _playlistId: Int = savedStateHandle.getOrThrow(PlaylistDetailsNav.KEY_MEDIA_ID)

    private val _cachedPoster = ArtworkCache.getPlaylistPoster(_playlistId)
    private val _cachedBackdrop = ArtworkCache.getPlaylistBackdrop(_playlistId)

    private var _detailedPlaylist: DetailedPlaylist? = null
    private val _localItems = mutableListOf<PlaylistItem>()

    private val _uiState = MutableStateFlow(
        PlaylistDetailsUIState(
            posterUrl = _cachedPoster,
            backdropUrl = _cachedBackdrop,
            castManager = castManager,
            onPopBackStack = ::popBackStack,
            onHideError = ::hideError,
            onPlayItem = ::playItem,
            onDeleteItem = ::deleteItem,
            onDeletePlaylist = ::deletePlaylist,
            onListUpdated = ::listUpdated,
            onNavToItem = ::navToItem,
            onPlayUpNext = ::playUpNext,
            onRenamePlaylist = ::renamePlaylist,
            onUpdateDownloads = ::updateDownloads,
            onUpdateListOnServer = ::updateListOnServer
        )
    )
    val uiState = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            PlayerStateManager.playbackEnded.collectLatest {
                updateData()
            }
        }

        viewModelScope.launch {
            downloadManager.currentDownloads.collectLatest { jobs ->
                updateDownloadStatus(jobs)
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        ArtworkCache.deletePlaylist(_playlistId)
    }



    private suspend fun updateData() {
        try {
            _detailedPlaylist = playlistRepository.details(_playlistId)

            val items = _detailedPlaylist!!.items ?: listOf()
            _localItems.addAll(items)
            val upNext = items.firstOrNull { it.id == _detailedPlaylist!!.currentItemId }
                ?: items.firstOrNull()

            if(_detailedPlaylist!!.artworkUrl != _cachedPoster ||
                _detailedPlaylist!!.backdropUrl != _cachedBackdrop) {
                _uiState.update {
                    it.copy(
                        posterUrl = _detailedPlaylist!!.artworkUrl,
                        backdropUrl = _detailedPlaylist!!.backdropUrl
                    )
                }
            }

            _uiState.update {
                it.copy(
                    playlistId = _detailedPlaylist!!.id,
                    loading = false,
                    title = _detailedPlaylist!!.name,
                    canPlay = items.isNotEmpty(),
                    upNextTitle = upNext?.title ?: "",
                    partiallyPlayed = _detailedPlaylist!!.currentProgress > 0.0,
                    items = items,
                    updateList = true
                )
            }
        } catch (ex: Exception) {
            setError(ex = ex, criticalError = true)
        }
    }

    private fun updateDownloadStatus(jobs: List<UIJob>) {
        var status = DownloadStatus.None
        var percent = 0f
        var cnt = 0

        val job = jobs.firstOrNull {
            it.mediaId == _playlistId && it.mediaType == MediaTypes.Playlist
        }
        if (job != null) {
            status = job.status
            cnt = job.count
            percent = when (cnt) {
                0 -> 1f
                else -> job.downloads.sumOf { it.percent.toDouble() }.toFloat() / cnt
            }
        }
        _uiState.update {
            it.copy(
                downloadStatus = status,
                currentDownloadCount = cnt,
                downloadPercent = percent
            )
        }
    }

    private fun setError(ex: Exception, criticalError: Boolean) {
        ex.logToCrashlytics()
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

    private fun hideError() {
        if (_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }

    private fun renamePlaylist(newName: String) {
        _uiState.update {
            it.copy(
                busy = true
            )
        }
        viewModelScope.launch {
            try {
                playlistRepository.rename(
                    UpdatesPlaylist(
                        id = _detailedPlaylist!!.id,
                        name = newName
                    )
                )
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
    }

    private fun updateDownloads(newCount: Int) {
        viewModelScope.launch {
            if (newCount == 0)
                downloadManager.delete(_detailedPlaylist!!.id, MediaTypes.Playlist)
            else
                downloadManager.addOrUpdatePlaylist(_detailedPlaylist!!, newCount)
        }
    }

    private fun listUpdated() {
        _uiState.update {
            it.copy(updateList = false)
        }
    }

    private fun updateListOnServer(from: Int, to: Int) {

        _uiState.update {
            it.copy(busy = true)
        }

        viewModelScope.launch {
            try {
                playlistRepository.moveItem(_detailedPlaylist!!.items!![from].id, to)
                _detailedPlaylist!!.items = _detailedPlaylist!!.items!!.toMutableList().apply {
                    add(to, removeAt(from))
                }
                _uiState.update {
                    it.copy(busy = false)
                }
            } catch (ex: Exception) {
                ex.logToCrashlytics()
                _localItems.clear()
                _localItems.addAll(_detailedPlaylist!!.items ?: listOf())
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

    private fun deleteItem(id: Int) {
        _localItems.remove(_localItems.first { it.id == id })
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
                _detailedPlaylist!!.items = _detailedPlaylist!!.items!!.toMutableList().apply {
                    remove(_detailedPlaylist!!.items!!.first { it.id == id })
                }
                _uiState.update {
                    it.copy(busy = false)
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun deletePlaylist() {
        viewModelScope.launch {
            try {
                playlistRepository.deletePlaylist(_detailedPlaylist!!.id)
                downloadManager.delete(_detailedPlaylist!!.id, MediaTypes.Playlist)
                HomeViewModel.triggerUpdate()
                popBackStack()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    fun playUpNext() {
        val upNext = _detailedPlaylist!!.items!!.firstOrNull {
            it.id == _detailedPlaylist!!.currentItemId
        } ?: _detailedPlaylist!!.items!!.firstOrNull()
        playItem(upNext?.id ?: -1)
    }

    private fun playItem(id: Int) {
        navigateToRoute(
            PlayerNav.getRoute(
                mediaId = _playlistId,
                sourceType = PlayerNav.MEDIA_TYPE_PLAYLIST,
                upNextId = id
            )
        )
    }

    private fun navToItem(id: Int) {

        val pli = _detailedPlaylist!!.items?.first { it.id == id }!!
        ArtworkCache.add(pli)

        if (pli.mediaType == MediaTypes.Movie) {

            navigateToRoute(
                MovieDetailsNav.getRoute(
                    mediaId = pli.mediaId,
                    detailedPlaylistId = _playlistId,
                    fromPlaylist = true,
                    playlistUpNextIndex = pli.index
                )
            )
        } else if (pli.mediaType == MediaTypes.Episode) {
            navigateToRoute(
                EpisodeDetailsNav.getRoute(
                    parentId = _playlistId,
                    mediaId = pli.mediaId,
                    canPlay = true,
                    source = EpisodeDetailsNav.SOURCE_PLAYLIST_DETAILS,
                    playlistUpNextIndex = pli.index
                )
            )
        }
    }
}