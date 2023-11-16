package tv.dustypig.dustypig.ui.main_app.screens.player

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.ExternalSubtitle
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.api.repositories.MoviesRepository
import tv.dustypig.dustypig.api.repositories.PlaylistRepository
import tv.dustypig.dustypig.api.repositories.SeriesRepository
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastConnectionState
import tv.dustypig.dustypig.global_managers.cast_manager.CastConnectionStateListener
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import java.lang.Long.max
import java.util.Calendar
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule
import kotlin.math.abs


@SuppressLint("SimpleDateFormat")
@UnstableApi
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val mediaRepository: MediaRepository,
    private val moviesRepository: MoviesRepository,
    private val seriesRepository: SeriesRepository,
    private val playlistRepository: PlaylistRepository,
    private val downloadManager: DownloadManager,
    private val castManager: CastManager,
    app: Application,
    settingsManager: SettingsManager,
    savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator, Player.Listener, CastConnectionStateListener {

    companion object {
        private const val TAG = "PlayerViewModel"
    }

    private data class StartInfo(
        val index:Int,
        val milliSeconds:Long
    )

    private val _uiState = MutableStateFlow(
        PlayerUIState(
            castManager = castManager,
            onPopBackStack = ::popBackStack,
            onPlayNext = ::playNext,
            onSkipIntro = ::skipIntro
        )
    )
    val uiState: StateFlow<PlayerUIState> = _uiState.asStateFlow()

    private val _mediaId: Int = savedStateHandle.getOrThrow(PlayerNav.KEY_MEDIA_ID)
    private val _mediaType: Int = savedStateHandle.getOrThrow(PlayerNav.KEY_MEDIA_TYPE)
    private var _upNextId: Int = savedStateHandle.getOrThrow(PlayerNav.KEY_UPNEXT_ID)


    private val _localPlayer = ExoPlayer
        .Builder(app)
        .build().also {
            it.addListener(this)
            it.playWhenReady = true
        }

    private val _timer = Timer()
    private var _timerBusy = false
    private val _videoTimings = arrayListOf<VideoTiming>()
    private var _autoSkipIntros = false
    private var _autoSkipCredits = false
    private var _previousSeconds = 0.0
    private var _mediaQueue = arrayListOf<MediaItem>()
    private var _currentMediaItemId: String? = null



    init {
        PlayerStateManager.playerCreated()

        viewModelScope.launch {
            _autoSkipIntros = settingsManager.getSkipIntros()
            _autoSkipCredits = settingsManager.getSkipCredits()
        }

        _uiState.update {
            it.copy(
                player = _localPlayer
            )
        }


        //Apparently there is no listener for currentPosition, so poll it with a timer
        _timer.schedule(
            delay = 0,
            period = 100
        ) {
            timerTick()
        }

        castManager.addListener(this)
        onConnectionStateChanged(castManager.castState.value.castConnectionState)

        viewModelScope.launch {
            castManager.castState.collectLatest { castState ->
                _uiState.update {
                    it.copy(
                        currentItemTitle = castState.title,
                        castPlaybackStatus = castState.playbackStatus,
                        castHasPrevious = castState.hasPrevious,
                        castHasNext = castState.hasNext,
                        castPosition = castState.position,
                        castDuration = castState.duration
                    )
                }
            }
        }
    }


    override fun popBackStack() {
        _timer.cancel()
        _localPlayer.stop()
        _localPlayer.release()
        _mediaQueue.clear()
        castManager.removeListener(this)
        PlayerStateManager.playerDisposed()
        HomeViewModel.triggerUpdate()
        routeNavigator.popBackStack()
    }



    // CastConnectionStateListener

    override fun onConnectionStateChanged(castConnectionState: CastConnectionState) {
        when (castConnectionState) {
            CastConnectionState.Connected -> {
                _uiState.update {
                    it.copy(isCastPlayer = true)
                }
                switchPlayer()
            }

            CastConnectionState.Unavailable,
            CastConnectionState.Disconnected -> {
                _uiState.update {
                    it.copy(isCastPlayer = false)
                }
                switchPlayer()
            }

            else -> {}
        }
    }


    //Player.Listener (ExoPlayer)

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
        try {
            if (mediaItem == null) {
                _currentMediaItemId = null
            } else {

                _currentMediaItemId = mediaItem.mediaId

                val videoTiming = _videoTimings.first {
                    it.mediaId == _currentMediaItemId
                }
                videoTiming.introClicked = false
                videoTiming.creditsClicked = false
                _uiState.update {
                    it.copy(
                        currentItemTitle = mediaItem.mediaMetadata.title.toString()
                    )
                }
            }
            Log.d(TAG, "onMediaItemTransition: _currentMediaItemId=$_currentMediaItemId")
        } catch (ex: Exception) {
            Log.e(TAG, "onMediaItemTransition", ex)
        }
    }






    //Internal functions

//    private fun navBack() {
//        _timer.cancel()
//        _localPlayer.stop()
//        _localPlayer.release()
//        _mediaQueue.clear()
//        castManager.removeListener(this)
//        PlayerStateManager.playerDisposed()
//        HomeViewModel.triggerUpdate()
//        popBackStack()
//    }

    private fun skipIntro() {
        try {
            val videoTiming = _videoTimings.first {
                it.mediaId == _currentMediaItemId
            }
            videoTiming.introClicked = true
            _localPlayer.seekTo((videoTiming.introEndTime ?: 0).toLong() * 1000)
        } catch (ex: Exception) {
            _uiState.update {
                it.copy(
                    showErrorDialog = true,
                    errorMessage = ex.localizedMessage
                )
            }
        }
    }

    private fun playNext() {
        _uiState.update {
            it.copy(
                showErrorDialog = false
            )
        }
        try {
            val videoTiming = _videoTimings.first {
                it.mediaId == _currentMediaItemId
            }
            videoTiming.creditsClicked = true
            if (_localPlayer.hasNextMediaItem()) {
                _localPlayer.seekToNextMediaItem()
            } else {
                popBackStack()
            }
        } catch (ex: Exception) {
            _uiState.update {
                it.copy(
                    showErrorDialog = true,
                    errorMessage = ex.localizedMessage
                )
            }
        }
    }

    private fun switchPlayer() {
        _uiState.update {
            it.copy(busy = true)
        }

        viewModelScope.launch {
            try {
                _localPlayer.stop()
                _localPlayer.clearMediaItems()

                _mediaQueue.clear()
                _videoTimings.clear()

                if (_uiState.value.isCastPlayer) {
                    when (_mediaType) {
                        PlayerNav.MEDIA_TYPE_MOVIE -> castManager.playMovie(_mediaId)
                        PlayerNav.MEDIA_TYPE_SERIES -> castManager.playSeries(_mediaId, _upNextId)
                        else -> castManager.playPlaylist(_mediaId, _upNextId)
                    }
                } else {
                    val startInfo = when (_mediaType) {
                        PlayerNav.MEDIA_TYPE_MOVIE -> loadMovie()
                        PlayerNav.MEDIA_TYPE_SERIES -> loadSeries()
                        else -> loadPlaylist()
                    }
                    _localPlayer.setMediaItems(_mediaQueue, startInfo.index, startInfo.milliSeconds)
                    _localPlayer.playWhenReady = true
                    _localPlayer.prepare()
                }

                //On first load, the upNext is used. When switching between cast and local,
                //allow the loader to specify resume position
                _upNextId = -1

            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        showErrorDialog = true,
                        errorMessage = ex.localizedMessage
                    )
                }
            }

            _uiState.update {
                it.copy(busy = false)
            }
        }
    }

    private suspend fun loadMovie(): StartInfo {
        val detailedMovie = moviesRepository.details(_mediaId)

        val calendar = Calendar.getInstance()
        calendar.time = detailedMovie.date

        _videoTimings.add(
            VideoTiming(
                mediaId = _mediaId.toString(),
                introStartTime = detailedMovie.introStartTime,
                introEndTime = detailedMovie.introEndTime,
                creditsStartTime = detailedMovie.creditStartTime,
                isMovie = true
            )
        )


        _mediaQueue.add(
            MediaItem
                .Builder()
                .setMediaId(detailedMovie.id.toString())
                .setUri(tryGetLocalVideo(detailedMovie.id, detailedMovie.videoUrl!!))
                .addSubs(detailedMovie.id, detailedMovie.externalSubtitles)
                .setMediaMetadata(
                    MediaMetadata
                        .Builder()
                        .setArtworkUri(tryGetLocalPoster(detailedMovie.id, detailedMovie.artworkUrl).toUri())
                        .setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE)
                        .setTitle(detailedMovie.title)
                        .setReleaseYear(calendar.get(Calendar.YEAR))
                        .setReleaseMonth(calendar.get(Calendar.MONTH))
                        .setRecordingDay(calendar.get(Calendar.DAY_OF_MONTH))
                        .build()
                )
                .build()
        )

        return StartInfo(index = 0, milliSeconds = convertPlayedToMs(detailedMovie.played))
    }

    private suspend fun loadSeries(): StartInfo {
        val detailedSeries = seriesRepository.details(_mediaId)

        var upNextId = _upNextId
        if(upNextId < 0) {
            upNextId = detailedSeries.episodes?.firstOrNull {
                it.upNext
            }?.id ?: -1
        }

        var currentItemIndex = 0
        var playbackPositionMs = 0L

        detailedSeries.episodes!!.forEach { ep ->

            _videoTimings.add(
                VideoTiming(
                    mediaId = ep.id.toString(),
                    introStartTime = ep.introStartTime,
                    introEndTime = ep.introEndTime,
                    creditsStartTime = ep.creditStartTime,
                    isMovie = false
                )
            )

            _mediaQueue.add(
                MediaItem
                    .Builder()
                    .setMediaId(ep.id.toString())
                    .setUri(tryGetLocalVideo(ep.id,  ep.videoUrl))
                    .addSubs(ep.id, ep.externalSubtitles)
                    .setMediaMetadata(
                        MediaMetadata
                            .Builder()
                            .setArtworkUri(tryGetLocalPoster(detailedSeries.id, detailedSeries.artworkUrl).toUri())
                            .setMediaType(MediaMetadata.MEDIA_TYPE_TV_SHOW)
                            .setTitle(ep.title)
                            .setSubtitle(ep.seriesTitle)
                            .setDiscNumber(ep.seasonNumber.toInt())
                            .setTrackNumber(ep.episodeNumber.toInt())
                            .build()
                    )
                    .build()
            )

            if (ep.id == upNextId) {
                currentItemIndex = _mediaQueue.count() - 1
                playbackPositionMs = convertPlayedToMs(ep.played)
            }
        }

        return StartInfo(index = currentItemIndex, milliSeconds =  playbackPositionMs)
    }

    private suspend fun loadPlaylist(): StartInfo {
        val detailedPlaylist = playlistRepository.details(_mediaId)

        var upNextId = _upNextId
        if(upNextId < 0) {
            upNextId = detailedPlaylist.items?.firstOrNull {
                it.id == detailedPlaylist.currentItemId
            }?.id ?: -1
        }


        var currentItemIndex = 0
        var playbackPositionMs = 0L

        detailedPlaylist.items?.forEach { pli ->

            _videoTimings.add(
                VideoTiming(
                    mediaId = pli.id.toString(),
                    introStartTime = pli.introStartTime,
                    introEndTime = pli.introEndTime,
                    creditsStartTime = pli.creditStartTime,
                    isMovie = false
                )
            )

            _mediaQueue.add(
                MediaItem
                    .Builder()
                    .setMediaId(tryGetLocalVideo(pli.id, pli.id.toString()))
                    .setUri(pli.videoUrl)
                    .addSubs(pli.id, pli.externalSubtitles)
                    .setMediaMetadata(
                        MediaMetadata
                            .Builder()
                            .setArtworkUri(tryGetLocalPoster(detailedPlaylist.id, detailedPlaylist.artworkUrl).toUri())
                            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                            .setTitle(pli.title)
                            .setSubtitle(detailedPlaylist.name)
                            .build()
                    )
                    .build()
            )
            if (pli.id == upNextId) {
                currentItemIndex = _mediaQueue.count() - 1
                playbackPositionMs = convertPlayedToMs(detailedPlaylist.currentProgress)
            }
        }
        return StartInfo(index = currentItemIndex, milliSeconds = playbackPositionMs)
    }


    private fun tryGetLocalVideo(id: Int, videoUrl: String): String {
        var ext = videoUrl.split("?")[0]
        ext = ext.substring(ext.lastIndexOf("."))
        return downloadManager.getLocalVideo(id, ext) ?: videoUrl
    }

    private fun tryGetLocalPoster(id: Int, posterUrl: String): String {
        var ext = posterUrl.split("?")[0]
        ext = ext.substring(ext.lastIndexOf("."))
        return downloadManager.getLocalPoster(id, ext) ?: posterUrl
    }

    private fun MediaItem.Builder.addSubs(id: Int, subTitles: List<ExternalSubtitle>?): MediaItem.Builder {
        if(!subTitles.isNullOrEmpty()) {
            val subtitleConfigurations = arrayListOf<MediaItem.SubtitleConfiguration>()
            for (sub in subTitles) {

                var subExt = sub.url.split("?")[0]
                subExt = subExt.substring(subExt.lastIndexOf("."))
                subExt = "$sub.name.$subExt"
                val url = downloadManager.getLocalSubtitle(id, subExt) ?: sub.url

                subtitleConfigurations.add(
                    MediaItem.SubtitleConfiguration
                        .Builder(url.toUri())
                        .setLabel(sub.name)
                        .build()
                )
            }
            this.setSubtitleConfigurations(subtitleConfigurations)
        }
        return this
    }

    private fun convertPlayedToMs(played: Double?) = max(((played ?: 0.0) * 1000).toLong(), 0)

    private fun timerTick() {

        if(_uiState.value.isCastPlayer) {
            return
        }

        if (_timerBusy) {
            return
        }
        _timerBusy = true

        viewModelScope.launch {
            try {

                if(_localPlayer.playbackState == Player.STATE_ENDED) {
                    popBackStack()
                } else {

                    val seconds = _localPlayer.currentPosition.toDouble() / 1000
                    val videoTiming = _videoTimings.first {
                        it.mediaId == _currentMediaItemId!!
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

                    val endOfPlaylist = _videoTimings.indexOf(videoTiming) == _videoTimings.size - 1
                    if(!endOfPlaylist) {
                        if (videoTiming.creditsClicked) {
                            if (_uiState.value.currentPositionWithinCredits) {
                                _uiState.update {
                                    it.copy(
                                        currentPositionWithinCredits = false
                                    )
                                }
                            }
                        } else {
                            val length = _localPlayer.contentDuration.toDouble() / 1000
                            val positionWithinCredits =
                                videoTiming.positionWithinCredits(seconds, length)
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
                    }


                    if(_currentMediaItemId != null && seconds > 1.0 && abs(_previousSeconds - seconds) >= 1.0) {
                        _previousSeconds = seconds
                        val pp = PlaybackProgress(
                            id = _currentMediaItemId!!.toInt(),
                            seconds = seconds
                        )

                        when(_mediaType) {
                            PlayerNav.MEDIA_TYPE_MOVIE,
                            PlayerNav.MEDIA_TYPE_SERIES ->
                                mediaRepository.updatePlaybackProgress(pp)

                            PlayerNav.MEDIA_TYPE_PLAYLIST ->
                                playlistRepository.setPlaylistProgress(pp)
                        }
                    }
                }
            } catch (ex: Exception) {
                //Log.e(TAG, "timerTick", ex)
            }

            _timerBusy = false
        }
    }

}