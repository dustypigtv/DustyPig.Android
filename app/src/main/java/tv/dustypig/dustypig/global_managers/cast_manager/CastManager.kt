package tv.dustypig.dustypig.global_managers.cast_manager

import android.content.Context
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import com.google.android.gms.cast.MediaError
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueData
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tv.dustypig.dustypig.global_managers.AuthManager
import javax.inject.Inject
import javax.inject.Singleton


@UnstableApi
@Singleton
class CastManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: AuthManager
) {

    private val tag = "CastManager"


    private var mediaRouter: MediaRouter? = null
    private val mediaCallback: MediaRouter.Callback = MediaRouterCallback(::refreshRoutes)
    private val mediaSelector = MediaRouteSelector
        .Builder()
        .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
        .build()
    private var remoteMediaClient: RemoteMediaClient? = null


    private val sessionListener = SessionListener(::setRemoteMediaClientAndInform)
    private val remoteMediaClientListener = RemoteMediaClientListener(
        updateProgress = ::updateProgress,
        updatePlaybackInfo = ::updatePlaybackInfo
    )

    private val _castState = MutableStateFlow(CastState())
    val castState = _castState.asStateFlow()

    private val connectionStateListeners = ArrayList<CastConnectionStateListener>()
    private val _castButtonState = MutableStateFlow(CastConnectionState.Unavailable)
    /**
     * Only used for cast button. Use CastConnectionStateListener in ViewModel
     */
    val castButtonState = _castButtonState.asStateFlow()



    init {
        try {
            val castContext: CastContext = CastContext.getSharedInstance(context){
                it.run()
            }.result
            mediaRouter = MediaRouter.getInstance(context)
            castContext.sessionManager.also {
                it.addSessionManagerListener(sessionListener, CastSession::class.java)
            }
            _castButtonState.update { CastConnectionState.Disconnected }
            Log.i(tag, "Cast available")
            Log.i(tag, CastOptionsProvider.receiverApplicationId(context))
        } catch (ex: Exception) {
            Log.e(tag, "init", ex)
            Log.i(tag, "Cast not available")
        }
    }


    /**
     * Call this before showing picker dialogs
     */
    fun setActiveScanning() {
        Log.d(tag, "setActiveScanning")
        mediaRouter?.addCallback(mediaSelector, mediaCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN)
    }

    /**
     * Call this from Activity.onStart & after dismissing picker dialogs
     */
    fun setPassiveScanning() {
        Log.d(tag, "setPassiveScanning")
        mediaRouter?.addCallback(mediaSelector, mediaCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
    }

    /**
     * Call this from Activity.onStop
     */
    fun stopScanning() {
        Log.d(tag, "stopScanning")
        mediaRouter?.addCallback(mediaSelector, mediaCallback, 0)
    }

    /**
     * Call this from Activity.onDestroy
     */
    fun destroy() {
        Log.d(tag, "destroy")
        mediaRouter?.removeCallback(mediaCallback)
    }


    fun addListener(castConnectionStateListener: CastConnectionStateListener) {
        Log.d(tag, "addListener")
        if(!connectionStateListeners.contains(castConnectionStateListener)) {
            connectionStateListeners.add(castConnectionStateListener)
        }
    }

    fun removeListener(castConnectionStateListener: CastConnectionStateListener) {
        Log.d(tag, "removeListener")
        if(connectionStateListeners.contains(castConnectionStateListener)) {
            connectionStateListeners.remove(castConnectionStateListener)
        }
    }

    fun disconnect() {
        Log.d(tag, "disconnect")
        mediaRouter?.defaultRoute?.select()
    }

    fun togglePlayPause() {
        Log.d(tag, "togglePlayPause")
        if(remoteMediaClient?.isPaused == true) {
            remoteMediaClient?.play()
        } else {
            remoteMediaClient?.pause()
        }
    }

    fun playNext() {
        Log.d(tag, "playNext")
        remoteMediaClient?.queueNext(null)
    }

    fun playPrevious() {
        Log.d(tag, "playPrevious")
        remoteMediaClient?.queuePrev(null)
    }

    fun seekBy(milliseconds: Long) {
        Log.d(tag, "seekBy: milliseconds=$milliseconds")
        val newPosition = (_castState.value.position + milliseconds)
            .coerceIn(0L, _castState.value.duration)
        seekTo(newPosition)
    }

    fun seekTo(milliseconds: Long) {
        Log.d(tag, "seekTo: milliseconds=$milliseconds")
        remoteMediaClient?.seek(
            MediaSeekOptions
                .Builder()
                .setPosition(milliseconds)
                .build()
        )
    }

    fun playMovie(movieId: Int) {
        Log.d(tag, "playMovie: movieId=$movieId")
        playMedia("dustypig://movie/$movieId")
    }

    fun playSeries(seriesId: Int, upNextId: Int) {
        Log.d(tag, "playMovie: seriesId=$seriesId, upNextId=$upNextId")
        playMedia("dustypig://series/$seriesId/$upNextId")
    }

    fun playPlaylist(playlistId: Int, upNextId: Int) {
        Log.d(tag, "playMovie: playlistId=$playlistId, upNextId=$upNextId")
        playMedia("dustypig://playlist/$playlistId/$upNextId")
    }

    private fun playMedia(url: String) {
        remoteMediaClient?.load(
            MediaLoadRequestData
                .Builder()
                .setCredentials(authManager.currentToken)
                .setQueueData(
                    MediaQueueData
                        .Builder()
                        .setEntity(url)
                        .build()
                )
                .build()
        )
    }




    private fun refreshRoutes() {
        try {
            val routes = mediaRouter?.routes?.toMutableList() ?: mutableListOf()
            var i = routes.size
            while (i-- > 0) {
                val include = !routes[i].isDefault
                        && !routes[i].isBluetooth
                        && routes[i].isEnabled
                        && routes[i].matchesSelector(mediaSelector)
                if (!include) {
                    routes.removeAt(i)
                }
            }
            routes.sortWith { o1, o2 -> o1!!.name.compareTo(o2!!.name, ignoreCase = true) }

            val selectedRoute: RouteInfo? = mediaRouter?.selectedRoute

            _castState.update {
                it.copy(
                    availableRoutes = routes,
                    selectedRoute = selectedRoute
                )
            }
        } catch(ex: Exception) {
            Log.e(tag, ex.localizedMessage, ex)
        }
    }

    private fun setRemoteMediaClientAndInform(
        newRemoteMediaClient: RemoteMediaClient?,
        castConnectionState: CastConnectionState
    ) {
        updateProgress(0, 0)

        /*
            When a session is connected, call setRemoteMediaClient before updating the state,
            so state listeners can immediately use it.

            Otherwise, tell state listeners to stop using it before setting it to null
         */
        if(castConnectionState == CastConnectionState.Connected) {
            setRemoteMediaClient(newRemoteMediaClient)
            informConnectionState(castConnectionState)
        } else {
            informConnectionState(castConnectionState)
            setRemoteMediaClient(newRemoteMediaClient)
        }

        updatePlaybackInfo()
    }

    private fun setRemoteMediaClient(newRemoteMediaClient: RemoteMediaClient?) {
        if (remoteMediaClient == newRemoteMediaClient) {
            return
        }

        if (remoteMediaClient != null) {
            remoteMediaClient?.unregisterCallback(remoteMediaClientListener)
            remoteMediaClient?.removeProgressListener(remoteMediaClientListener)
        }

        remoteMediaClient = newRemoteMediaClient
        if(remoteMediaClient != null) {
            remoteMediaClient?.registerCallback(remoteMediaClientListener)
            remoteMediaClient?.addProgressListener(remoteMediaClientListener, 1000)
        }

        updatePlaybackInfo()
        updateProgress(position = 0L, duration = 0L)
    }

    private fun informConnectionState(castConnectionState: CastConnectionState) {
        Log.d(tag, "informConnectionState: castConnectionState=$castConnectionState")
        _castButtonState.update { castConnectionState }
        connectionStateListeners.forEach { listener ->
            try {
                listener.onConnectionStateChanged(castConnectionState)
            } catch(ex: Exception) {
                Log.e(tag, "CastConnectionStateListener.onConnectionStateChanged", ex)
            }
        }
    }

    private fun updateProgress(position: Long, duration: Long) {
        val forceZero = remoteMediaClient == null
        val coercedDuration = (if (forceZero) 0L else duration).coerceAtLeast(minimumValue = 0L)
        val coercedPosition = (if (forceZero) 0L else position).coerceIn(
            minimumValue = 0L,
            maximumValue = coercedDuration
        )

        _castState.update {
            it.copy(
                duration = coercedDuration,
                position = coercedPosition,
                progress =
                if (coercedDuration > 0L)
                    coercedPosition.toFloat() / coercedDuration.toFloat()
                else
                    0f
            )
        }
    }


    private fun updatePlaybackInfo() {
        try {
            var status = CastPlaybackStatus.Stopped
            if (remoteMediaClient?.isBuffering == true) {
                status = CastPlaybackStatus.Buffering
            } else if (remoteMediaClient?.isLoadingNextItem == true) {
                status = CastPlaybackStatus.Buffering
            } else if (remoteMediaClient?.isPaused == true) {
                status = CastPlaybackStatus.Paused
            } else if (remoteMediaClient?.isPlaying == true) {
                status = CastPlaybackStatus.Playing
            }

            val itemIds = remoteMediaClient?.mediaStatus?.queueItems?.map { it.itemId } ?: listOf()
            val currentItemIndex = itemIds.indexOf(remoteMediaClient?.mediaStatus?.currentItemId ?: 0)
            val hasNext = itemIds.size > 1 && currentItemIndex < itemIds.size - 1
            val hasPrev = itemIds.size > 1 && currentItemIndex > 0

            var title: String? = null
            var artworkUrl: String? = null
            val mqi = remoteMediaClient?.currentItem
            if (mqi != null) {
                val metadata = mqi.media?.metadata
                if (metadata != null) {
                    title = metadata.getString(MediaMetadata.KEY_TITLE)
                    artworkUrl = metadata.images.firstOrNull()?.url?.toString()
                }
            }

            _castState.update {
                it.copy(
                    playbackStatus = status,
                    hasPrevious = hasPrev,
                    hasNext = hasNext,
                    title = title,
                    artworkUrl = artworkUrl
                )
            }
        } catch (ex: Exception) {
            Log.e(tag, "updatePlaybackInfo", ex)
        }
    }


    private class SessionListener(
        private val setRemoteMediaClientAndInform: (RemoteMediaClient?, CastConnectionState) -> Unit
    ): SessionManagerListener<CastSession> {

        private val tag = "CM.SessionListener"

        override fun onSessionStarted(castSession: CastSession, sessionId: String) {
            Log.d(tag, "onSessionStarted")
            setRemoteMediaClientAndInform(castSession.remoteMediaClient, CastConnectionState.Connected)
        }

        override fun onSessionResumed(castSession: CastSession, wasSuspended: Boolean) {
            Log.d(tag, "onSessionResumed")
            setRemoteMediaClientAndInform(castSession.remoteMediaClient, CastConnectionState.Connected)
        }

        override fun onSessionEnded(castSession: CastSession, error: Int) {
            Log.d(tag, "onSessionEnded")
            setRemoteMediaClientAndInform(null, CastConnectionState.Disconnected)
        }

        override fun onSessionSuspended(castSession: CastSession, reason: Int) {
            Log.d(tag, "onSessionSuspended")
            setRemoteMediaClientAndInform(null, CastConnectionState.Disconnected)
        }

        override fun onSessionEnding(castSession: CastSession) {
            Log.d(tag, "onSessionEnding")
            setRemoteMediaClientAndInform(null, CastConnectionState.Busy)
        }

        override fun onSessionResumeFailed(castSession: CastSession, error: Int) {
            Log.d(tag, "onSessionResumeFailed")
            setRemoteMediaClientAndInform(null, CastConnectionState.Disconnected)
        }

        override fun onSessionResuming(castSession: CastSession, sessionId: String) {
            Log.d(tag, "onSessionResuming")
            setRemoteMediaClientAndInform(null, CastConnectionState.Busy)
        }

        override fun onSessionStartFailed(castSession: CastSession, error: Int) {
            Log.d(tag, "onSessionStartFailed")
            setRemoteMediaClientAndInform(null, CastConnectionState.Disconnected)
        }

        override fun onSessionStarting(castSession: CastSession) {
            Log.d(tag, "onSessionStarting")
            setRemoteMediaClientAndInform(null, CastConnectionState.Busy)
        }
    }


    private class RemoteMediaClientListener(
        private val updateProgress: (position: Long, duration: Long) -> Unit,
        private val updatePlaybackInfo: () -> Unit
    ): RemoteMediaClient.ProgressListener, RemoteMediaClient.Callback() {

        private val tag = "CM.RMCListener"

        override fun onProgressUpdated(position: Long, duration: Long) {
            Log.d(tag, "onProgressUpdated: position=$position, duration=$duration")
            updateProgress(position, duration)
        }

        override fun onPreloadStatusUpdated() {
            super.onPreloadStatusUpdated()
            Log.d(tag, "onPreloadStatusUpdated")
            updatePlaybackInfo()
        }

        override fun onStatusUpdated() {
            super.onStatusUpdated()
            Log.d(tag, "onStatusUpdated")
            updatePlaybackInfo()
        }

        override fun onQueueStatusUpdated() {
            super.onQueueStatusUpdated()
            Log.d(tag, "onQueueStatusUpdated")
            updatePlaybackInfo()
        }

        override fun onMediaError(mediaError: MediaError) {
            super.onMediaError(mediaError)
            Log.e(tag, "onMediaError", Exception(mediaError.reason ?: "Unknown Error"))
        }

        override fun onMetadataUpdated() {
            super.onMetadataUpdated()
            Log.d(tag, "onMetadataUpdated")
            updatePlaybackInfo()
        }
    }


    private class MediaRouterCallback(
        private val refreshRoute: () -> Unit
    ): MediaRouter.Callback() {

        private val tag = "CM.MediaRouterCallback"

        override fun onRouteAdded(router: MediaRouter, route: RouteInfo) {
            super.onRouteAdded(router, route)
            Log.d(tag, "onRouteAdded")
            refreshRoute()
        }

        override fun onRouteRemoved(router: MediaRouter, route: RouteInfo) {
            super.onRouteRemoved(router, route)
            Log.d(tag, "onRouteRemoved")
            refreshRoute()
        }

        override fun onRouteChanged(router: MediaRouter, route: RouteInfo) {
            super.onRouteChanged(router, route)
            Log.d(tag, "onRouteChanged")
            refreshRoute()
        }

        override fun onRouteSelected(
            router: MediaRouter,
            route: RouteInfo,
            reason: Int
        ) {
            super.onRouteSelected(router, route, reason)
            Log.d(tag, "onRouteSelected")
            refreshRoute()
        }

        override fun onRouteUnselected(
            router: MediaRouter,
            route: RouteInfo,
            reason: Int
        ) {
            super.onRouteUnselected(router, route, reason)
            Log.d(tag, "onRouteUnselected")
            refreshRoute()
        }

        override fun onProviderAdded(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
            super.onProviderAdded(router, provider)
            Log.d(tag, "onProviderAdded")
            refreshRoute()
        }

        override fun onProviderChanged(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
            super.onProviderChanged(router, provider)
            Log.d(tag, "onProviderChanged")
            refreshRoute()
        }

        override fun onProviderRemoved(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
            super.onProviderRemoved(router, provider)
            Log.d(tag, "onProviderRemoved")
            refreshRoute()
        }

        override fun onRoutePresentationDisplayChanged(
            router: MediaRouter,
            route: RouteInfo
        ) {
            super.onRoutePresentationDisplayChanged(router, route)
            Log.d(tag, "onRoutePresentationDisplayChanged")
            refreshRoute()
        }
    }
}