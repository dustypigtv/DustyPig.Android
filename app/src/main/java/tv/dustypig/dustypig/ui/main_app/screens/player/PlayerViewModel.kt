package tv.dustypig.dustypig.ui.main_app.screens.player

import android.annotation.SuppressLint
import android.app.Application
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
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.api.repositories.MoviesRepository
import tv.dustypig.dustypig.api.repositories.PlaylistRepository
import tv.dustypig.dustypig.api.repositories.SeriesRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics
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
    private val authManager: AuthManager,
    private val mediaRepository: MediaRepository,
    private val moviesRepository: MoviesRepository,
    private val seriesRepository: SeriesRepository,
    private val playlistRepository: PlaylistRepository,
    //private val downloadManager: DownloadManager,
    private val castManager: CastManager,
    app: Application,
    settingsManager: SettingsManager,
    savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator, Player.Listener {

    private data class StartInfo(
        val index:Int,
        val milliSeconds:Long
    )

    private val _uiState = MutableStateFlow(
        PlayerUIState(
            castManager = castManager,
            onPopBackStack = ::navBack,
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
    private var _firstLoad = true
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

        viewModelScope.launch {
            try {
                castManager.state.collectLatest { castState ->
                    if (_firstLoad || (_uiState.value.isCastPlayer != castState.castAvailable)) {
                        _firstLoad = false
                        _uiState.update {
                            it.copy(
                                isCastPlayer = castState.connected
                            )
                        }
                        switchPlayer()
                    }
                    _uiState.update {
                        it.copy(
                            currentItemTitle = castState.title,
                            castPaused = castState.paused,
                            castBuffering = castState.buffering,
                            castHasPrevious = castState.hasPrevious,
                            castHasNext = castState.hasNext,
                            castPosition = castState.position,
                            castDuration = castState.duration.coerceAtLeast(0f)
                        )
                    }
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
    }







    //Player.Listener

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
        } catch (_: Exception) {
        }
    }






    //Internal functions

    private fun navBack() {
        release()
        HomeViewModel.triggerUpdate()
        routeNavigator.popBackStack()
    }

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


    private suspend fun switchPlayer() {

        _uiState.update {
            it.copy(busy = true)
        }

        _localPlayer.stop()
        _localPlayer.clearMediaItems()

        _mediaQueue.clear()
        _videoTimings.clear()

        if(_uiState.value.isCastPlayer) {
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
            _localPlayer.prepare()
        }

        //On first load, the upNext is used. When switching between cast and local,
        //allow the loader to specify resume position
        _upNextId = -1

        _uiState.update {
            it.copy(busy = false)
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

        // Store TOKEN in mediaItem.mediaMetadata.description
        _mediaQueue.add(
            MediaItem
                .Builder()
                .setMediaId(_mediaId.toString())
                .setUri(detailedMovie.videoUrl!!)
                .setMediaMetadata(
                    MediaMetadata
                        .Builder()
                        .setArtworkUri(detailedMovie.artworkUrl.toUri())
                        .setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE)
                        .setTitle(detailedMovie.title)
                        .setReleaseYear(calendar.get(Calendar.YEAR))
                        .setReleaseMonth(calendar.get(Calendar.MONTH))
                        .setRecordingDay(calendar.get(Calendar.DAY_OF_MONTH))
                        .setDescription(authManager.currentToken)
                        .build()
                )
                .build()
        )

        return StartInfo(index = 0, milliSeconds = convertPlayedToMs(detailedMovie.played))
    }

    private suspend fun loadSeries(): StartInfo {
        val detailedSeries = seriesRepository.details(_mediaId)
        var currentItemIndex = 0
        var playbackPositionMs = 0L

        detailedSeries.episodes!!.forEach { ep ->

            _videoTimings.add(
                VideoTiming(
                    mediaId = _mediaId.toString(),
                    introStartTime = ep.introStartTime,
                    introEndTime = ep.introEndTime,
                    creditsStartTime = ep.creditStartTime,
                    isMovie = false
                )
            )

            /*
            * Store Series Title in mediaItem.mediaMetadata.subtitle
            * Store Season Number in mediaItem.mediaMetadata.discNumber
            * Store Episode Number in mediaItem.mediaMetadata.trackNumber
            * Store TOKEN in mediaItem.mediaMetadata.description
            */
            _mediaQueue.add(
                MediaItem
                    .Builder()
                    .setMediaId(_mediaId.toString())
                    .setUri(ep.videoUrl)
                    .setMediaMetadata(
                        MediaMetadata
                            .Builder()
                            .setArtworkUri(detailedSeries.artworkUrl.toUri())
                            .setMediaType(MediaMetadata.MEDIA_TYPE_TV_SHOW)
                            .setTitle(ep.title)
                            .setSubtitle(ep.seriesTitle)
                            .setDiscNumber(ep.seasonNumber.toInt())
                            .setTrackNumber(ep.episodeNumber.toInt())
                            .setDescription(authManager.currentToken)
                            .build()
                    )
                    .build()
            )

            if (ep.id == _upNextId) {
                currentItemIndex = _mediaQueue.count() - 1
                playbackPositionMs = convertPlayedToMs(ep.played)
            }
        }

        return StartInfo(index = currentItemIndex, milliSeconds =  playbackPositionMs)
    }

    private suspend fun loadPlaylist(): StartInfo {
        val detailedPlaylist = playlistRepository.details(_mediaId)
        var currentItemIndex = 0
        var playbackPositionMs = 0L

        detailedPlaylist.items?.forEach { pli ->

            _videoTimings.add(
                VideoTiming(
                    mediaId = _mediaId.toString(),
                    introStartTime = pli.introStartTime,
                    introEndTime = pli.introEndTime,
                    creditsStartTime = pli.creditStartTime,
                    isMovie = false
                )
            )

            _mediaQueue.add(
                MediaItem
                    .Builder()
                    .setMediaId(_mediaId.toString())
                    .setUri(pli.videoUrl)
                    .setMediaMetadata(
                        MediaMetadata
                            .Builder()
                            .setArtworkUri(detailedPlaylist.artworkUrl.toUri())
                            .setMediaType(MediaMetadata.MEDIA_TYPE_TV_SHOW)
                            .setTitle(pli.title)
                            .setSubtitle(detailedPlaylist.name)
                            .setDescription(authManager.currentToken)
                            .build()
                    )
                    .build()
            )
            if (pli.id == _upNextId) {
                currentItemIndex = _mediaQueue.count() - 1
                playbackPositionMs = convertPlayedToMs(detailedPlaylist.currentProgress)
            }
        }
        return StartInfo(index = currentItemIndex, milliSeconds = playbackPositionMs)
    }


    private fun release() {
        _timer.cancel()

        _localPlayer.stop()
        _mediaQueue.clear()
        _localPlayer.release()

        PlayerStateManager.playerDisposed()
    }
//
//    private fun addMediaItem(
//        id: Int,
//        displayTitle: String?,
//        videoUrl: String,
//        subTitles: List<ExternalSubtitle>?,
//        introStartTime: Double?,
//        introEndTime: Double?,
//        creditsStartTime: Double?,
//        isMovie: Boolean
//    ) {
//
//        val builder = MediaItem
//            .Builder()
//            .setMediaId(id.toString())
//            .setMediaMetadata(
//                MediaMetadata
//                    .Builder()
//                    .setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE)
//                    .setTitle(displayTitle)
//                    .build()
//            )
//
//        var videoExt = videoUrl.split("?")[0]
//        videoExt = videoExt.substring(videoExt.lastIndexOf("."))
//        var localUrl = downloadManager.getLocalVideo(id, videoExt) ?: videoUrl
//        builder.setUri(videoUrl)
//
//        if(!subTitles.isNullOrEmpty()) {
//            val subtitleConfigurations = arrayListOf<MediaItem.SubtitleConfiguration>()
//            for (sub in subTitles) {
//
//                var subExt = sub.url.split("?")[0]
//                subExt = subExt.substring(subExt.lastIndexOf("."))
//                localUrl = downloadManager.getLocalSubtitle(id, subExt) ?: sub.url
//
//                subtitleConfigurations.add(
//                    MediaItem.SubtitleConfiguration
//                        .Builder(localUrl.toUri())
//                        .setLabel(sub.name)
//                        .build()
//                )
//            }
//            builder.setSubtitleConfigurations(subtitleConfigurations)
//        }
//        _mediaQueue.add(builder.build())
//
//        _videoTimings.add(
//            VideoTiming(
//                mediaId = id.toString(),
//                introStartTime = introStartTime,
//                introEndTime = introEndTime,
//                creditsStartTime = creditsStartTime,
//                isMovie = isMovie
//            )
//        )
//    }

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

                    if(videoTiming.creditsClicked) {
                        if(_uiState.value.currentPositionWithinCredits) {
                            _uiState.update {
                                it.copy(
                                    currentPositionWithinCredits = true
                                )
                            }
                        }
                    } else {
                        val length = _localPlayer.contentDuration.toDouble() / 1000
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



                    if(seconds > 1.0 && abs(_previousSeconds - seconds) >= 1.0) {
                        _previousSeconds = seconds

                        when(_mediaType) {
                            PlayerNav.MEDIA_TYPE_MOVIE,
                            PlayerNav.MEDIA_TYPE_SERIES ->
                                mediaRepository.updatePlaybackProgress(
                                    PlaybackProgress(
                                        id = _currentMediaItemId!!.toInt(),
                                        seconds = seconds
                                    )
                                )


                            PlayerNav.MEDIA_TYPE_PLAYLIST ->
                                playlistRepository.setPlaylistProgress(
                                    PlaybackProgress(
                                        id = _currentMediaItemId!!.toInt(),
                                        seconds = seconds
                                    )
                                )
                        }
                    }
                }
            } catch (_: Exception) {
            }

            _timerBusy = false
        }
    }
}