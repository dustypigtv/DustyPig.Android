package tv.dustypig.dustypig.ui.main_app.screens.player

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.models.SetPlaylistProgress
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.api.repositories.PlaylistRepository
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import java.lang.Long.max
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule
import kotlin.math.abs

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val player: Player,
    private val mediaRepository: MediaRepository,
    private val playlistRepository: PlaylistRepository,
): ViewModel(), RouteNavigator by routeNavigator  {

    companion object {
        private const val TAG = "PlayerViewModel"

        var mediaType = MediaTypes.Movie
        var upNextId = 0

        var detailedMovie: DetailedMovie? = null
        var detailedSeries: DetailedSeries? = null
        var detailedEpisode: DetailedEpisode? = null
        var detailedPlaylist: DetailedPlaylist? = null
    }

    private val _uiState = MutableStateFlow(PlayerUIState())
    val uiState: StateFlow<PlayerUIState> = _uiState.asStateFlow()

    private val timer = Timer()
    private var timerBusy = false
    private var lastReportedTime = 0.0

    init {

        PlayerStateManager.playerCreated()

        _uiState.update {
            it.copy(
                player = player
            )
        }

        try {
            when (mediaType) {
                MediaTypes.Movie -> {
                    player.setMediaItem(
                        MediaItem
                            .Builder()
                            .setMediaId(detailedMovie!!.id.toString())
                            .setUri(detailedMovie!!.videoUrl)
                            .setMediaMetadata(
                                MediaMetadata
                                    .Builder()
                                    .setArtworkUri(detailedMovie!!.artworkUrl.toUri())
                                    .setDescription(detailedMovie!!.description)
                                    .setDisplayTitle(detailedMovie!!.displayTitle())
                                    .build()
                            )
                            .build(),
                        max(((detailedMovie!!.played ?: 0.0) * 1000).toLong(), 0)
                    )
                }


                MediaTypes.Episode -> {
                    player.setMediaItem(
                        MediaItem
                            .Builder()
                            .setMediaId(detailedEpisode!!.id.toString())
                            .setUri(detailedEpisode!!.videoUrl)
                            .setMediaMetadata(
                                MediaMetadata
                                    .Builder()
                                    .setArtworkUri(detailedEpisode!!.artworkUrl.toUri())
                                    .setDescription(detailedEpisode!!.description)
                                    .setDisplayTitle(detailedEpisode!!.title)
                                    .build()
                            )
                            .build(),
                        max(((detailedEpisode!!.played ?: 0.0) * 1000).toLong(), 0)
                    )
                }

                MediaTypes.Series -> {
                    var found = false
                    var first = true
                    detailedSeries!!.episodes?.forEach { ep ->
                        if (ep.id == upNextId) {
                            found = true
                        }
                        if (found) {
                            val mi = MediaItem
                                .Builder()
                                .setMediaId(ep.id.toString())
                                .setUri(ep.videoUrl)
                                .setMediaMetadata(
                                    MediaMetadata
                                        .Builder()
                                        .setArtworkUri(ep.artworkUrl.toUri())
                                        .setDescription(ep.description)
                                        .setDisplayTitle(ep.title)
                                        .build()
                                )
                                .build()
                            if (first) {
                                player.setMediaItem(mi, max(((ep.played ?: 0.0) * 1000).toLong(), 0))
                            } else {
                                player.addMediaItem(mi)
                            }

                            first = false
                        }
                    }
                }


                MediaTypes.Playlist -> {
                    var found = false
                    var first = true
                    detailedPlaylist!!.items?.forEach { pli ->
                        if (pli.id == upNextId) {
                            found = true
                        }
                        if (found) {
                            val mi = MediaItem
                                .Builder()
                                .setMediaId(pli.id.toString())
                                .setUri(pli.videoUrl)
                                .setMediaMetadata(
                                    MediaMetadata
                                        .Builder()
                                        .setArtworkUri(pli.artworkUrl.toUri())
                                        .setDescription(pli.description)
                                        .setDisplayTitle(pli.title)
                                        .build()
                                )
                                .build()
                            if (first) {
                                player.setMediaItem(mi, max(((pli.played ?: 0.0) * 1000).toLong(), 0))
                            } else {
                                player.addMediaItem(mi)
                            }

                            first = false
                        }
                    }
                }
            }

            if (player.mediaItemCount > 0) {
                player.prepare()
                player.play()
            } else {
                throw Exception("No playable items found")
            }

            timer.schedule(
                delay = 0,
                period = 1000
            ){
                timerTick()
            }

        } catch (ex: Exception) {
            ex.logToCrashlytics()
            _uiState.update {
                it.copy(
                    showErrorDialog = true,
                    errorMessage = ex.localizedMessage
                )
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }

    fun hideError() {
        popBackStack()
    }

    private fun timerTick() {

        if (timerBusy) {
            return
        }
        timerBusy = true

        viewModelScope.launch {
            try {

                if(player.playbackState == Player.STATE_ENDED) {
                    popBackStack()
                } else {

                    //Don't constantly update if paused
                    val seconds = player.currentPosition.toDouble() / 1000
                    if(abs(seconds - lastReportedTime) >= 0.1) {
                        lastReportedTime = seconds
                        when (mediaType) {
                            MediaTypes.Movie, MediaTypes.Series, MediaTypes.Episode -> mediaRepository.updatePlaybackProgress(
                                PlaybackProgress(
                                    id = player.currentMediaItem!!.mediaId.toInt(),
                                    seconds = seconds
                                )
                            )

                            MediaTypes.Playlist -> playlistRepository.setPlaylistProgress(
                                SetPlaylistProgress(
                                    playlistId = detailedPlaylist!!.id,
                                    newIndex = player.currentMediaItemIndex,
                                    newProgress = seconds
                                )
                            )
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.e(TAG, "timerTick", ex)
            }

            timerBusy = false
        }
    }

    override fun popBackStack() {
        player.release()
        HomeViewModel.triggerUpdate()
        PlayerStateManager.playerDisposed()
        routeNavigator.popBackStack()
    }
}