package tv.dustypig.dustypig.ui.main_app.screens.player

import android.app.Application
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.ExternalSubtitle
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.models.SetPlaylistProgress
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.api.repositories.PlaylistRepository
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.media_cache_manager.MediaCacheManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import java.lang.Long.max
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule
import kotlin.math.abs


@UnstableApi
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val mediaRepository: MediaRepository,
    private val playlistRepository: PlaylistRepository,
    private val downloadManager: DownloadManager,
    app: Application,
    settingsManager: SettingsManager,
    savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator, Player.Listener, SessionAvailabilityListener {

    companion object {
        private const val TAG = "PlayerViewModel"
    }

    private val _uiState = MutableStateFlow(PlayerUIState())
    val uiState: StateFlow<PlayerUIState> = _uiState.asStateFlow()

    private val _cacheId: String = savedStateHandle.getOrThrow(PlayerNav.KEY_CACHE_ID)
    private val _sourceType: Int = savedStateHandle.getOrThrow(PlayerNav.KEY_SOURCE_TYPE)
    private val _upNextId: Int = savedStateHandle.getOrThrow(PlayerNav.KEY_UPNEXT_ID)



    private var _localPlayer = ExoPlayer
        .Builder(app)
        .build()

    private val _timer = Timer()
    private var _timerBusy = false
    private val _itemsWithSubtitles = arrayListOf<String>()
    private val _videoTimings = arrayListOf<VideoTiming>()
    private var _autoSkipIntros = false
    private var _autoSkipCredits = false
    private var _playing = false
    private var _previousSeconds = 0.0
    private lateinit var _castPlayer: CastPlayer
    private var _currentPlayer: Player
    private var _localMediaQueue = arrayListOf<MediaItem>()
    private val _castMediaQueue = arrayListOf<MediaItem>()
    private var _playlistId = 0

    init {
        PlayerStateManager.playerCreated()

        viewModelScope.launch {
            _autoSkipIntros = settingsManager.getSkipIntros()
            _autoSkipCredits = settingsManager.getSkipCredits()
        }

        _localPlayer.addListener(this)
        _localPlayer.playWhenReady = true
        _localPlayer.prepare()
        _currentPlayer = _localPlayer

        try {
            _castPlayer = CastPlayer(CastContext.getSharedInstance()!!)
            _castPlayer.addListener(this)
            _castPlayer.setSessionAvailabilityListener(this)
            _castPlayer.playWhenReady = true
            _castPlayer.prepare()
            if (_castPlayer.isCastSessionAvailable) {
                _currentPlayer = _castPlayer
            }
        } catch (_: Exception) {
        }

        _uiState.update {
            it.copy(
                player = _currentPlayer
            )
        }


        try {
            var playbackPositionMs = 0L
            var currentItemIndex = 0

            when(_sourceType) {
                PlayerNav.SOURCE_TYPE_MOVIE -> {
                    val detailedMovie = MediaCacheManager.Movies[_cacheId]!!
                    addMediaItem(
                        detailedMovie.id,
                        detailedMovie.displayTitle(),
                        detailedMovie.videoUrl!!,
                        detailedMovie.externalSubtitles,
                        detailedMovie.introStartTime,
                        detailedMovie.introEndTime,
                        detailedMovie.creditStartTime,
                        isMovie = true
                    )
                    playbackPositionMs = convertPlayedToMs(detailedMovie.played)
                }

                PlayerNav.SOURCE_TYPE_SERIES -> {
                    val detailedSeries = MediaCacheManager.Series[_cacheId]!!
                    detailedSeries.episodes?.forEach { ep ->
                        addMediaItem(
                            ep.id,
                            ep.fullDisplayTitle(),
                            ep.videoUrl,
                            ep.externalSubtitles,
                            ep.introStartTime,
                            ep.introEndTime,
                            ep.creditStartTime,
                            isMovie = false
                        )

                        if(ep.id == _upNextId){
                            currentItemIndex = _localMediaQueue.count() - 1
                            playbackPositionMs = convertPlayedToMs(ep.played)
                        }

                    }
                }

                PlayerNav.SOURCE_TYPE_PLAYLIST -> {
                    val detailedPlaylist = MediaCacheManager.Playlists[_cacheId]!!
                    _playlistId = detailedPlaylist.id
                    detailedPlaylist.items?.forEach { pli ->
                        addMediaItem(
                            pli.mediaId,
                            pli.title,
                            pli.videoUrl,
                            pli.externalSubtitles,
                            pli.introStartTime,
                            pli.introEndTime,
                            pli.creditStartTime,
                            isMovie = pli.mediaType == MediaTypes.Movie
                        )

                        if(pli.index == _upNextId){
                            currentItemIndex = _localMediaQueue.count() - 1
                            playbackPositionMs = convertPlayedToMs(pli.played)
                        }
                    }
                }
            }


            if (_localMediaQueue.isEmpty()) {
                throw Exception("No playable items found")
            }

            setMediaQueue(currentItemIndex, playbackPositionMs)

            //Apparently there is no listener for currentPosition, so poll it with a timer
            _timer.schedule(
                delay = 0,
                period = 100
            ) {
                timerTick()
            }

        }  catch (ex: Exception) {
            ex.logToCrashlytics()
            _uiState.update {
                it.copy(
                    showErrorDialog = true,
                    errorMessage = ex.localizedMessage
                )
            }
        }

    }


    private fun release() {
        _timer.cancel()
        _currentPlayer.stop()

        _castMediaQueue.clear()
        try {
            _castPlayer.setSessionAvailabilityListener(null)
            _castPlayer.release()
        }
        catch(_: Exception) {
        }

        _localMediaQueue.clear()
        _localPlayer.release()

        PlayerStateManager.playerDisposed()
    }

    private fun addMediaItem(
        mediaId: Int,
        title: String,
        videoUrl: String,
        subTitles: List<ExternalSubtitle>?,
        introStartTime: Double?,
        introEndTime: Double?,
        creditsStartTime: Double?,
        isMovie: Boolean
    ) {

        var ext = videoUrl.split("?")[0]
        ext = ext.substring(ext.lastIndexOf("."))


        //Cast is remote only
        var builder = MediaItem
            .Builder()
            .setMediaId(mediaId.toString())
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(title)
                    .build()
            )
            .setUri(videoUrl)
            .setMimeType("video/${ext.substring(1)}")
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
            builder.setSubtitleConfigurations(subtitleConfigurations)
            _itemsWithSubtitles.add(mediaId.toString())
        }
        _castMediaQueue.add(builder.build())



        //For local, check for downloaded files
        builder = MediaItem
            .Builder()
            .setMediaId(mediaId.toString())
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(title)
                    .build()
            )

        var localUrl = downloadManager.getLocalVideo(mediaId, ext) ?: videoUrl
        builder.setUri(localUrl)

        if(!subTitles.isNullOrEmpty()) {
            val subtitleConfigurations = arrayListOf<SubtitleConfiguration>()
            for (sub in subTitles) {

                ext = sub.url.split("?")[0]
                ext = ext.substring(ext.lastIndexOf("."))
                localUrl = downloadManager.getLocalVideo(mediaId, ext) ?: sub.url

                subtitleConfigurations.add(
                    SubtitleConfiguration
                        .Builder(localUrl.toUri())
                        .setLabel(sub.name)
                        .build()
                )
            }
            builder.setSubtitleConfigurations(subtitleConfigurations)
            _itemsWithSubtitles.add(mediaId.toString())
        }
        _localMediaQueue.add(builder.build())

        _videoTimings.add(
            VideoTiming(
                mediaId = mediaId.toString(),
                introStartTime = introStartTime,
                introEndTime = introEndTime,
                creditsStartTime = creditsStartTime,
                isMovie = isMovie
            )
        )


    }

    private fun setMediaQueue(itemIndex: Int, startPositionMs: Long) {
        if(_currentPlayer == _localPlayer)
            _currentPlayer.setMediaItems(_localMediaQueue, itemIndex, startPositionMs)
        else
            _currentPlayer.setMediaItems(_castMediaQueue, itemIndex, startPositionMs)
    }

    private fun convertPlayedToMs(played: Double?) = max(((played ?: 0.0) * 1000).toLong(), 0)

    private fun timerTick() {

        if (_timerBusy) {
            return
        }
        _timerBusy = true

        viewModelScope.launch {
            try {

                if(_currentPlayer.playbackState == Player.STATE_ENDED) {
                    popBackStack()
                } else {

                    val currentMediaItem = _currentPlayer.currentMediaItem!!

                    val seconds = _currentPlayer.currentPosition.toDouble() / 1000

                    val videoTiming = _videoTimings.first {
                        it.mediaId == currentMediaItem.mediaId
                    }
                    if(videoTiming.introClicked) {
                        if(_uiState.value.currentPositionWithinIntro) {
                            _uiState.update {
                                it.copy(
                                    currentPositionWithinIntro = false
                                )
                            }
                        }
                    } else {
                        val positionWithinIntro = videoTiming.positionWithinIntro(seconds)
                        if (positionWithinIntro && _autoSkipIntros) {
                            skipIntro()
                        } else {
                            if (_uiState.value.currentPositionWithinIntro != positionWithinIntro) {
                                _uiState.update {
                                    it.copy(
                                        currentPositionWithinIntro = positionWithinIntro
                                    )
                                }
                            }
                        }
                    }

                    if(videoTiming.creditsClicked) {
                        if(_uiState.value.currentPositionWithinCredits) {
                            _uiState.update {
                                it.copy(
                                    currentPositionWithinCredits = true
                                )
                            }
                        }
                    } else {
                        val length = _currentPlayer.contentDuration.toDouble() / 1000
                        val positionWithinCredits = videoTiming.positionWithinCredits(seconds, length)
                        if (positionWithinCredits && _autoSkipCredits) {
                            playNext()
                        } else {
                            if (_uiState.value.currentPositionWithinCredits != positionWithinCredits) {
                                _uiState.update {
                                    it.copy(
                                        currentPositionWithinCredits = positionWithinCredits
                                    )
                                }
                            }
                        }
                    }



                    if(_playing && abs(_previousSeconds - seconds) >= 1.0) {
                        _previousSeconds = seconds

                        when(_sourceType) {
                            PlayerNav.SOURCE_TYPE_MOVIE,
                            PlayerNav.SOURCE_TYPE_SERIES ->
                                mediaRepository.updatePlaybackProgress(
                                    PlaybackProgress(
                                        id = currentMediaItem.mediaId.toInt(),
                                        seconds = seconds
                                    )
                                )


                            PlayerNav.SOURCE_TYPE_PLAYLIST ->
                                playlistRepository.setPlaylistProgress(
                                    SetPlaylistProgress(
                                        playlistId = _playlistId,
                                        newIndex = _currentPlayer.currentMediaItemIndex,
                                        newProgress = seconds
                                    )
                                )
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.e(TAG, "timerTick", ex)
            }

            _timerBusy = false
        }
    }

    override fun popBackStack() {
        release()
        HomeViewModel.triggerUpdate()
        routeNavigator.popBackStack()
    }

    fun skipIntro() {
        try {
            val currentMediaItem = _currentPlayer.currentMediaItem!!
            val videoTiming = _videoTimings.first {
                it.mediaId == currentMediaItem.mediaId
            }
            videoTiming.introClicked = true
            _currentPlayer.seekTo((videoTiming.introEndTime ?: 0) .toLong() * 1000)
        } catch (_: Exception) {
        }
    }

    fun playNext() {
        _uiState.update {
            it.copy(
                showErrorDialog = false
            )
        }
        try {
            val currentMediaItem = _currentPlayer.currentMediaItem!!
            val videoTiming = _videoTimings.first {
                it.mediaId == currentMediaItem.mediaId
            }
            videoTiming.creditsClicked = true
            if(_currentPlayer.hasNextMediaItem()) {
                _currentPlayer.seekToNextMediaItem()
            } else {
                popBackStack()
            }
        } catch (_: Exception) {
            popBackStack()
        }
    }

    override fun onCastSessionAvailable() {
        setCurrentPlayer(_castPlayer)
    }

    override fun onCastSessionUnavailable() {
        setCurrentPlayer(_localPlayer)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        _playing = isPlaying
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        _uiState.update {
            it.copy(
                showErrorDialog = true,
                errorMessage = error.localizedMessage
            )
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        if (mediaItem != null) {
            val videoTiming = _videoTimings.first {
                it.mediaId == mediaItem.mediaId
            }
            videoTiming.introClicked = false
            videoTiming.creditsClicked = false
            _uiState.update {
                it.copy(
                    currentItemTitle = mediaItem.mediaMetadata.title.toString(),
                    currentItemHasSubtitles = _itemsWithSubtitles.contains(mediaItem.mediaId)
                )
            }
        }
    }

    private fun setCurrentPlayer(newPlayer: Player) {
        if (_currentPlayer === newPlayer) {
            return
        }

        _uiState.update {
            it.copy(
                player = _currentPlayer
            )
        }

        var playbackPositionMs = C.TIME_UNSET
        var currentItemIndex = C.INDEX_UNSET
        if (_currentPlayer.playbackState != Player.STATE_ENDED) {
            playbackPositionMs = _currentPlayer.currentPosition
            currentItemIndex = _currentPlayer.currentMediaItemIndex
        }

        _currentPlayer.stop()
        _currentPlayer.clearMediaItems()

        _currentPlayer = newPlayer
        setMediaQueue(currentItemIndex, playbackPositionMs)
    }
}