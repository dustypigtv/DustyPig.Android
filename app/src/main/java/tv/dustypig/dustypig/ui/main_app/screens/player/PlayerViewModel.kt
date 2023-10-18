package tv.dustypig.dustypig.ui.main_app.screens.player

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.SubtitleConfiguration
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
import tv.dustypig.dustypig.api.models.ExternalSubtitle
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
    private val itemsWithSubtitles = arrayListOf<String>()

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
                        buildMediaItem(
                            detailedMovie!!.id,
                            detailedMovie!!.videoUrl,
                            detailedMovie!!.externalSubtitles,
                            itemsWithSubtitles
                        ),
                        convertPlayedToMs(detailedMovie!!.played)
                    )
                }


                MediaTypes.Episode -> {
                    player.setMediaItem(
                        buildMediaItem(
                            detailedEpisode!!.id,
                            detailedEpisode!!.videoUrl,
                            detailedEpisode!!.externalSubtitles,
                            itemsWithSubtitles
                        ),
                        convertPlayedToMs(detailedEpisode!!.played)
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
                            val mi = buildMediaItem(
                                ep.id,
                                ep.videoUrl,
                                ep.externalSubtitles,
                                itemsWithSubtitles
                            )
                            if (first) {
                                player.setMediaItem(mi, convertPlayedToMs(ep.played))
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
                            val mi = buildMediaItem(
                                /* for playlists, mediaId = playlistItem.Index */
                                pli.index,
                                pli.videoUrl,
                                pli.externalSubtitles,
                                itemsWithSubtitles
                            )
                            if (first) {
                                player.setMediaItem(mi, convertPlayedToMs(pli.played))
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

    private fun buildMediaItem(
        mediaId: Int,
        videoUrl: String?,
        subTitles: List<ExternalSubtitle>?,
        itemsWithSubtitles: ArrayList<String>
    ): MediaItem {
        val ret = MediaItem
            .Builder()
            .setMediaId(mediaId.toString())
            .setUri(videoUrl)
        if(!subTitles.isNullOrEmpty()) {
            val subtitleConfigurations = arrayListOf<SubtitleConfiguration>()
            for (sub in subTitles) {
                subtitleConfigurations.add(
                    SubtitleConfiguration
                        .Builder(sub.url.toUri())
                        .setLabel(sub.name)
                        .build()
                )
            }
            ret.setSubtitleConfigurations(subtitleConfigurations)
            itemsWithSubtitles.add(mediaId.toString())
        }
        return ret.build()
    }

    private fun convertPlayedToMs(played: Double?) = max(((played ?: 0.0) * 1000).toLong(), 0)

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

                    val currentMediaItem = player.currentMediaItem!!

                    val showSubtitlesButton = itemsWithSubtitles.contains(currentMediaItem.mediaId)
                    if(showSubtitlesButton != _uiState.value.showSubtitlesButton) {
                        _uiState.update {
                            it.copy(
                                showSubtitlesButton = showSubtitlesButton
                            )
                        }
                    }

                    //Don't constantly update if paused
                    val seconds = player.currentPosition.toDouble() / 1000
                    if(abs(seconds - lastReportedTime) >= 0.1) {
                        lastReportedTime = seconds
                        when (mediaType) {
                            MediaTypes.Movie,
                            MediaTypes.Series,
                            MediaTypes.Episode -> mediaRepository.updatePlaybackProgress(
                                PlaybackProgress(
                                    id = currentMediaItem.mediaId.toInt(),
                                    seconds = seconds
                                )
                            )

                            MediaTypes.Playlist -> playlistRepository.setPlaylistProgress(
                                SetPlaylistProgress(
                                    playlistId = detailedPlaylist!!.id,

                                    /* for playlists, mediaId = playlistItem.Index */
                                    newIndex = currentMediaItem.mediaId.toInt(),
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