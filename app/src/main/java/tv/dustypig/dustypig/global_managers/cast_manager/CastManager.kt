package tv.dustypig.dustypig.global_managers.cast_manager

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
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


    private val mediaRouter = MediaRouter.getInstance(context)
    private val mediaCallback: MediaRouter.Callback = MediaRouterCallback(::refreshRoutes)
    private val mediaSelector = MediaRouteSelector
        .Builder()
        .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
        .build()

   private val _state = MutableStateFlow(
        CastState(
            defaultRoute = mediaRouter.defaultRoute,
            selectedRoute = mediaRouter.selectedRoute
        )
    )
    val state = _state.asStateFlow()


    private val remoteMediaClientListener = RemoteMediaClientListener(::updateProgress, ::updateInfo)
    private var remoteMediaClient: RemoteMediaClient? = null


    val castContext: CastContext = CastContext.getSharedInstance(context){
        it.run()
    }.result

    private val sessionListener = SessionListener(::setRemoteMediaClient)


    init {
        castContext.sessionManager.also {
            it.addSessionManagerListener(sessionListener, CastSession::class.java)
        }
    }


    /**
     * Call this before showing picker dialogs
     */
    fun setActiveScanning() {
        mediaRouter.addCallback(mediaSelector, mediaCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN)
    }


    /**
     * Call this from Activity.onStart & after dismissing picker dialogs
     */
    fun setPassiveScanning() {
        mediaRouter.addCallback(mediaSelector, mediaCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
    }

    /**
     * Call this from Activity.onStop
     */
    fun stopScanning() {
        mediaRouter.addCallback(mediaSelector, mediaCallback, 0)
    }

    /**
     * Call this from Activity.onDestroy
     */
    fun destroy() {
        mediaRouter.removeCallback(mediaCallback)
    }



    fun disconnect() {
        mediaRouter.unselect(MediaRouter.UNSELECT_REASON_STOPPED)
    }

    fun togglePlayPause() {
        if(remoteMediaClient == null) {
            return
        }
        if(remoteMediaClient?.isPaused == true) {
            remoteMediaClient?.play()
        } else {
            remoteMediaClient?.pause()
        }
    }

    fun playNext() {
        if(_state.value.hasNext)
            remoteMediaClient?.queueNext(null)
    }

    fun playPrevious() {
        if(_state.value.hasPrevious)
            remoteMediaClient?.queuePrev(null)
    }

    fun seekBy(seconds: Float) {
        val newPosition = (_state.value.position + seconds).coerceIn(0f, _state.value.duration - 1)
        seekTo(newPosition)
    }

    fun seekTo(seconds: Float) {
        remoteMediaClient?.seek(
            MediaSeekOptions
                .Builder()
                .setPosition((seconds * 1000).toLong())
                .build()
        )
    }

    fun playMovie(id: Int) {
        playMedia("myapp://movie/$id")
    }

    fun playSeries(seriesId: Int, upNextId: Int) {
        playMedia("myapp://series/$seriesId/$upNextId")
    }

    fun playPlaylist(playlistId: Int, upNextId: Int) {
        playMedia("myapp://playlist/$playlistId/$upNextId")
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


    private fun setRemoteMediaClient(newRemoteMediaClient: RemoteMediaClient?) {
        if (remoteMediaClient == newRemoteMediaClient) {
            return
        }

        if (remoteMediaClient != null) {
            remoteMediaClient?.unregisterCallback(remoteMediaClientListener)
            remoteMediaClient?.removeProgressListener(remoteMediaClientListener)
        }

        _state.update {
            it.copy(
                connected = false,
                playingContent = false,
                hasInfo = false
            )
        }
        remoteMediaClient = newRemoteMediaClient
        if (remoteMediaClient != null) {
            remoteMediaClient?.registerCallback(remoteMediaClientListener)
            remoteMediaClient?.addProgressListener(remoteMediaClientListener, 1000)
        }
    }

    private fun updateProgress(position: Float, duration: Float) = _state.update {
        it.copy(
            position = position,
            duration = duration,
            progress = if(duration > 0) position / duration else 0f
        )
    }

    private fun updateInfo() {
        val paused = remoteMediaClient?.isPaused ?: false
        val buffering = remoteMediaClient?.isBuffering ?: false

        val itemIds = remoteMediaClient?.mediaQueue?.itemIds ?: IntArray(0)
        val currentItemId = remoteMediaClient?.mediaStatus?.currentItemId ?: 0
        val hasNext = itemIds.size > 1 && itemIds.indexOf(currentItemId) < itemIds.size - 1
        val hasPrev = itemIds.size > 1 && itemIds.indexOf(currentItemId) > 0

        val mqi = remoteMediaClient?.currentItem
        if(mqi == null) {
            _state.update {
                it.copy(
                    playingContent = false,
                    hasInfo = false,
                    title = null,
                    progress = 0f,
                    artworkUrl = null,
                    paused = paused,
                    buffering = buffering,
                    hasPrevious = hasPrev,
                    hasNext =  hasNext
                )
            }
        } else {
            val metadata = mqi.media?.metadata
            if(metadata == null) {
                _state.update {
                    it.copy(
                        playingContent = true,
                        hasInfo = false,
                        title = null,
                        artworkUrl = null,
                        paused = paused,
                        buffering = buffering,
                        hasPrevious = hasPrev,
                        hasNext =  hasNext
                    )
                }
            }
            else {
                val title = metadata.getString(MediaMetadata.KEY_TITLE)
                _state.update {
                    it.copy(
                        playingContent = true,
                        hasInfo = true,
                        title = title,
                        artworkUrl = metadata.images.firstOrNull()?.url?.toString(),
                        paused = paused,
                        buffering = buffering,
                        hasPrevious = hasPrev,
                        hasNext =  hasNext
                    )
                }
            }
        }
    }

    private fun refreshRoutes() {

        val routes = mediaRouter.routes.toMutableList()
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

        val selectedRoute = mediaRouter.selectedRoute
        val isRemote = !(
                selectedRoute.isDefault //Not casting
                        || selectedRoute.isBluetooth //Speaker
                )

        val connected =
            if(isRemote) {
                selectedRoute.connectionState == MediaRouter.RouteInfo.CONNECTION_STATE_CONNECTED
            } else {
                false
            }

        _state.update {
            it.copy(
                castAvailable = routes.size > 0,
                availableRoutes = routes,
                selectedRoute = selectedRoute,
                connected = connected
            )
        }
    }



    private class SessionListener(
        private val setRemoteMediaClient: (RemoteMediaClient?) -> Unit
    ): SessionManagerListener<CastSession> {


        override fun onSessionStarted(castSession: CastSession, sessionId: String) {
            setRemoteMediaClient(castSession.remoteMediaClient)
        }

        override fun onSessionResumed(castSession: CastSession, wasSuspended: Boolean) {
            setRemoteMediaClient(castSession.remoteMediaClient)
        }

        override fun onSessionEnded(castSession: CastSession, error: Int) {
            setRemoteMediaClient(null)
        }

        override fun onSessionSuspended(castSession: CastSession, reason: Int) {
            setRemoteMediaClient(null)
        }

        // Unused
        override fun onSessionEnding(castSession: CastSession) { }
        override fun onSessionResumeFailed(castSession: CastSession, error: Int) { }
        override fun onSessionResuming(castSession: CastSession, sessionId: String) { }
        override fun onSessionStartFailed(castSession: CastSession, error: Int) { }
        override fun onSessionStarting(castSession: CastSession) { }
    }




    private class RemoteMediaClientListener(
        private val updateProgress: (Float, Float) -> Unit,
        private val updateInfo: () -> Unit
    ): RemoteMediaClient.ProgressListener, RemoteMediaClient.Callback() {

        override fun onProgressUpdated(position: Long, duration: Long) {
            updateProgress(position.toFloat() / 1000, duration.toFloat() / 1000)
        }

        override fun onStatusUpdated() {
            updateInfo()
        }

        override fun onQueueStatusUpdated() {
            updateInfo()
        }

    }




    private class MediaRouterCallback(
        private val refreshRoute: () -> Unit
    ): MediaRouter.Callback() {

        override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) {
            refreshRoute()
        }

        override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) {
            refreshRoute()
        }

        override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
            refreshRoute()
        }

        override fun onRouteSelected(
            router: MediaRouter,
            route: MediaRouter.RouteInfo,
            reason: Int
        ) {
            refreshRoute()
        }

        override fun onRouteUnselected(
            router: MediaRouter,
            route: MediaRouter.RouteInfo,
            reason: Int
        ) {
            refreshRoute()
        }

        override fun onProviderAdded(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
            refreshRoute()
        }

        override fun onProviderChanged(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
            refreshRoute()
        }

        override fun onProviderRemoved(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
            refreshRoute()
        }

        override fun onRoutePresentationDisplayChanged(
            router: MediaRouter,
            route: MediaRouter.RouteInfo
        ) {
            refreshRoute()
        }
    }
}