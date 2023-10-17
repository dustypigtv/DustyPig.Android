package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.nav.RouteNavigator
import java.lang.Long.max
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val player: Player
): ViewModel(), RouteNavigator by routeNavigator  {

    companion object {
        var mediaType = MediaTypes.Movie
        var upNextId = 0

        var detailedMovie: DetailedMovie? = null
        var detailedSeries: DetailedSeries? = null
        var detailedEpisode: DetailedEpisode? = null
        var detailedPlaylist: DetailedPlaylist? = null

        var playerScreenVisible by mutableStateOf(false)
            private set
    }

    private val _uiState = MutableStateFlow(PlayerUIState())
    val uiState: StateFlow<PlayerUIState> = _uiState.asStateFlow()

    init {

        playerScreenVisible = true

        _uiState.update {
            it.copy(
                player = player
            )
        }

        when (mediaType) {
            MediaTypes.Movie -> {
                player.setMediaItem(
                    MediaItem
                        .Builder()
                        .setMediaId("movie/${detailedMovie!!.id}")
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
                        .setMediaId("episode/${detailedEpisode!!.id}")
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
                    if(ep.id == upNextId) {
                        found = true
                    }
                    if(found) {
                        val mi = MediaItem
                            .Builder()
                            .setMediaId("series/${ep.id}")
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
                        if(first) {
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
                    if(pli.id == upNextId) {
                        found = true
                    }
                    if(found) {
                        val mi = MediaItem
                            .Builder()
                            .setMediaId("playlist/${pli.id}")
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
                        if(first) {
                            player.setMediaItem(mi, max(((pli.played ?: 0.0) * 1000).toLong(), 0))
                        } else {
                            player.addMediaItem(mi)
                        }

                        first = false
                    }
                }
            }
        }

        if(player.mediaItemCount > 0) {
            player.prepare()
            player.play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerScreenVisible = false
        player.release()
    }

}