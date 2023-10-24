package tv.dustypig.dustypig.ui.main_app.screens.playlist_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.media_cache_manager.MediaCacheManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val playlistRepository: PlaylistRepository,
    private val downloadManager: DownloadManager,
    savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(PlaylistDetailsUIState())
    val uiState = _uiState.asStateFlow()

    private val _basicCacheId: String = savedStateHandle.getOrThrow(PlaylistDetailsNav.KEY_CACHE_ID)
    private val _playlistId: Int = savedStateHandle.getOrThrow(PlaylistDetailsNav.KEY_MEDIA_ID)

    private lateinit var _detailedPlaylist: DetailedPlaylist
    private val _detailCacheId = UUID.randomUUID().toString()
    private val _localItems = mutableListOf<PlaylistItem>()

    init {
        val cachedInfo = MediaCacheManager.getBasicInfo(_basicCacheId)
        _uiState.update {
            it.copy(
                loading = true,
                posterUrl = cachedInfo.posterUrl,
                title = cachedInfo.title
            )
        }

        viewModelScope.launch {
            PlayerStateManager.playbackEnded.collectLatest {
                updateData()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        MediaCacheManager.BasicInfo.removeAll { it.cacheId == _basicCacheId }
        MediaCacheManager.Playlists.remove(_detailCacheId)
    }

    private fun updateData() {

        viewModelScope.launch {
            try{
                _detailedPlaylist = playlistRepository.details(_playlistId)
                MediaCacheManager.Playlists[_detailCacheId] = _detailedPlaylist

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
                        playlistId = _detailedPlaylist.id,
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

        viewModelScope.launch {
            downloadManager.downloads.collectLatest { jobs ->
                val job = jobs.firstOrNull { it.mediaId == _detailedPlaylist.id && it.mediaType == MediaTypes.Playlist }
                if(job == null) {
                    _uiState.update {
                        it.copy(
                            downloadStatus = DownloadStatus.None,
                            currentDownloadCount = 0
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            downloadStatus = job.status,
                            currentDownloadCount = job.count
                        )
                    }
                }
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

    fun renamePlaylist(newName: String = "") {
        _uiState.update {
            it.copy(
                busy = true
            )
        }
        viewModelScope.launch {
            try {
                playlistRepository.rename(
                    UpdatesPlaylist(
                        id = _detailedPlaylist.id,
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

    fun updateDownloads(newCount: Int) {
        viewModelScope.launch {
            if (newCount == 0)
                downloadManager.delete(_detailedPlaylist.id, MediaTypes.Playlist)
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
                ex.logToCrashlytics()
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
        viewModelScope.launch {
            try {
                playlistRepository.deletePlaylist(_detailedPlaylist.id)
                downloadManager.delete(_detailedPlaylist.id, MediaTypes.Playlist)
                HomeViewModel.triggerUpdate()
                popBackStack()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }


    fun playUpNext() {
       navigateToRoute(
            PlayerNav.getRoute(
                cacheId = _detailCacheId,
                sourceType = PlayerNav.SOURCE_TYPE_PLAYLIST,
                upNextId = _detailedPlaylist.currentIndex
            )
        )
    }

    fun playItem(index: Int) {
        navigateToRoute(
            PlayerNav.getRoute(
                cacheId = _detailCacheId,
                sourceType = PlayerNav.SOURCE_TYPE_PLAYLIST,
                upNextId = index
            )
        )
    }

    fun navToItem(id: Int) {

        val pli = _detailedPlaylist.items?.firstOrNull { it.id == id } ?: return

        val cacheId = MediaCacheManager.add(
            title = pli.title,
            posterUrl = "",
            backdropUrl = pli.artworkUrl
        )

        if (pli.mediaType == MediaTypes.Movie) {
            navigateToRoute(
                MovieDetailsNav.getRoute(
                    mediaId = pli.mediaId,
                    basicCacheId = cacheId,
                    detailedPlaylistCacheId = _detailCacheId,
                    fromPlaylist = true,
                    playlistUpNextIndex = pli.index
                )
            )
        } else if (pli.mediaType == MediaTypes.Episode) {
            navigateToRoute(
                EpisodeDetailsNav.getRoute(
                    mediaId = pli.mediaId,
                    basicCacheId = cacheId,
                    detailedCacheId = _detailCacheId,
                    canPlay = true,
                    fromSeriesDetails = false,
                    playlistUpNextIndex = pli.index
                )
            )
        }
    }
}