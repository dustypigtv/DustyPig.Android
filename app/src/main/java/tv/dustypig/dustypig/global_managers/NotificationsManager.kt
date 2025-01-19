package tv.dustypig.dustypig.global_managers

import android.content.Intent
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.Notification
import tv.dustypig.dustypig.api.models.NotificationTypes
import tv.dustypig.dustypig.api.repositories.NotificationsRepository
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.search.tmdb_details.TMDBDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.friend_details_settings.FriendDetailsSettingsNav
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private data class FCMAlertData (
    val id: Int,
    val profileId: Int,
    val mediaId: Int?,
    val mediaType: MediaTypes?,
    val notificationType: NotificationTypes,
    val friendshipId: Int?
)

@Singleton
class NotificationsManager @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val authManager: AuthManager
) {
    companion object {

        const val DATA_ID = "dp.id"
        const val DATA_PROFILE_ID = "dp.pid"

        private const val DATA_MEDIA_ID = "dp.mid"
        private const val DATA_MEDIA_TYPE = "dp.mt"
        private const val DATA_NOTIFICATION_TYPE = "dp.nt"
        private  const val DATA_FRIENDSHIP_ID = "dp.fid"

        private const val FIRESTORE_ALERTS_COLLECTION_PATH = "alerts"

        // These are used to move data from the static object to the instance
        private val _mutableUpdateFlow = MutableSharedFlow<String>(replay = 1)
        private val _mutableAlertFlow = MutableSharedFlow<FCMAlertData?>(replay = 1)
        private val _mutableMarkReadFlow = MutableSharedFlow<Int>(replay = 1)
        private val _mutableDeleteFlow = MutableSharedFlow<Int>(replay = 1)

        private val _mutableNavRouteFlow = MutableSharedFlow<String>(replay = 1)

        private val _notificationsFlow = MutableSharedFlow<List<Notification>>(replay = 1)

        fun triggerMarkAsRead(id: Int) {
            _mutableMarkReadFlow.tryEmit(id)
        }

        fun triggerDelete(id: Int) {
            _mutableDeleteFlow.tryEmit(id)
        }

        fun triggerUpdate() {
            _mutableUpdateFlow.tryEmit(UUID.randomUUID().toString())
        }

        fun handleNotificationTapped(intent: Intent) {
            try {
                val id = intent.getStringExtra(DATA_ID)?.toInt() ?: return
                if (id < 1)
                    return

                val profileId = intent.getStringExtra(DATA_PROFILE_ID)?.toInt() ?: return
                if (profileId < 1)
                    return

                val notificationType = NotificationTypes.getByVal(
                    intent.getStringExtra(DATA_NOTIFICATION_TYPE)
                ) ?: return

                val mediaId = intent.getStringExtra(DATA_MEDIA_ID)?.toInt()
                val mediaType = MediaTypes.getByVal(
                    intent.getStringExtra(DATA_MEDIA_TYPE)
                )

                val friendshipId = intent.getStringExtra(DATA_FRIENDSHIP_ID)?.toInt()

                val alert = FCMAlertData(
                    id = id,
                    profileId = profileId,
                    mediaId = mediaId,
                    mediaType = mediaType,
                    notificationType = notificationType,
                    friendshipId = friendshipId
                )

                //Move to instance where profileId can be checked against current user
                _mutableAlertFlow.tryEmit(alert)

            } catch (ex: Exception) {
                ex.logToCrashlytics()
            }
        }



        fun getNavRoute(
            notificationType: NotificationTypes,
            mediaId: Int? = null,
            mediaType: MediaTypes? = null,
            friendshipId: Int? = null
        ): String? {
            try {
                return when (notificationType) {
                    NotificationTypes.NewMediaRequested -> getTMDBRoute(mediaId!!, mediaType!!)
                    NotificationTypes.NewMediaPending -> getTMDBRoute(mediaId!!, mediaType!!)
                    NotificationTypes.NewMediaRejected -> getTMDBRoute(mediaId!!, mediaType!!)

                    NotificationTypes.NewMediaFulfilled -> getMediaRoute(mediaId!!, mediaType!!)
                    NotificationTypes.NewMediaAvailable -> getMediaRoute(mediaId!!, mediaType!!)
                    NotificationTypes.OverrideMediaRequested -> getMediaRoute(mediaId!!, mediaType!!)

                    NotificationTypes.OverrideMediaGranted -> getMediaRoute(mediaId!!, mediaType!!)
                    NotificationTypes.OverrideMediaRejected -> getMediaRoute(mediaId!!, mediaType!!)

                    NotificationTypes.FriendshipInvited -> FriendDetailsSettingsNav.getRouteForId(
                        friendshipId!!
                    )
                    NotificationTypes.FriendshipAccepted -> FriendDetailsSettingsNav.getRouteForId(
                        friendshipId!!
                    )
                }
            }
            catch (ex: Exception) {
                ex.logToCrashlytics()
                return null
            }
        }

        private fun getMediaRoute(
            mediaId: Int,
            mediaType: MediaTypes
        ): String? {
            return when (mediaType) {
                MediaTypes.Movie -> MovieDetailsNav.getRoute(
                    mediaId = mediaId,
                    basicCacheId = UUID.randomUUID().toString(),
                    detailedPlaylistCacheId = UUID.randomUUID().toString(),
                    fromPlaylist = false,
                    playlistUpNextIndex = 0
                )

                MediaTypes.Series -> SeriesDetailsNav.getRoute(
                    mediaId = mediaId,
                    basicCacheId = ""
                )

                MediaTypes.Episode -> EpisodeDetailsNav.getRoute(
                    mediaId = mediaId,
                    basicCacheId = UUID.randomUUID().toString(),
                    detailedCacheId = UUID.randomUUID().toString(),
                    canPlay = true,
                    fromSeriesDetails = false,
                    playlistUpNextIndex = -1
                )

                else -> null
            }
        }

        private fun getTMDBRoute(
            mediaId: Int,
            mediaType: MediaTypes
        ): String? {
            return when (mediaType) {
                MediaTypes.Movie -> TMDBDetailsNav.getRoute(
                    mediaId = mediaId,
                    cacheId = "",
                    isMovie = true
                )
                MediaTypes.Series -> TMDBDetailsNav.getRoute(
                    mediaId = mediaId,
                    cacheId = "",
                    isMovie = false
                )

                else -> null
            }
        }

    }


    val navRouteFlow = _mutableNavRouteFlow.asSharedFlow()
    val notifications = _notificationsFlow.asSharedFlow()

    private var listenerRegistration: ListenerRegistration? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _mutableAlertFlow.collect {
                alertTapped(it)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            _mutableMarkReadFlow.collect {
                markAsRead(it)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            _mutableDeleteFlow.collect {
                delete(it)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            _mutableUpdateFlow.collectLatest {
                loadData()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            authManager.loginState.collectLatest {
                listenerRegistration?.remove()
                listenerRegistration = Firebase.firestore
                    .collection(FIRESTORE_ALERTS_COLLECTION_PATH)
                    .document(authManager.currentProfileId.toString())
                    .addSnapshotListener{ _, _ ->
                        _mutableUpdateFlow.tryEmit(UUID.randomUUID().toString())
                    }
            }
        }
    }



    private suspend fun loadData() {
        if (authManager.loginState.value != AuthManager.LOGIN_STATE_LOGGED_IN) {
            _notificationsFlow.tryEmit(listOf())
            return
        }

        try {
            val lst = notificationsRepository.list()
            _notificationsFlow.tryEmit(lst)
        } catch (ex: Exception) {
            ex.logToCrashlytics()
        }
    }

    private fun alertTapped(alert: FCMAlertData?) {
        if (alert == null)
            return

        if (authManager.loginState.value != AuthManager.LOGIN_STATE_LOGGED_IN)
            return

        if (authManager.currentProfileId != alert.profileId)
            return

        triggerMarkAsRead(alert.id)

        val route = getNavRoute(
            notificationType = alert.notificationType,
            mediaId = alert.mediaId,
            mediaType = alert.mediaType,
            friendshipId = alert.friendshipId
        ) ?: return

        _mutableNavRouteFlow.tryEmit(route)
    }

    private suspend fun markAsRead(id: Int) {
        try {
            val lst: ArrayList<Notification> =
                _notificationsFlow.replayCache.firstOrNull()?.let { ArrayList(it) } ?: return
            if(lst.removeAll { it.id == id })
                _notificationsFlow.tryEmit(lst.toList())
            notificationsRepository.markAsRead(id)
        } catch (ex: Exception) {
            ex.logToCrashlytics()
        }
    }

    private suspend fun delete(id: Int) {
        try {
            val lst: ArrayList<Notification> =
                _notificationsFlow.replayCache.firstOrNull()?.let { ArrayList(it) } ?: return
            if(lst.removeAll { it.id == id })
                _notificationsFlow.tryEmit(lst.toList())
            notificationsRepository.delete(id)
        } catch (ex: Exception) {
            ex.logToCrashlytics()
        }
    }
}