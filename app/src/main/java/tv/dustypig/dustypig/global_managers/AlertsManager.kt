package tv.dustypig.dustypig.global_managers

import android.content.Intent
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.FCMToken
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.Notification
import tv.dustypig.dustypig.api.models.NotificationTypes
import tv.dustypig.dustypig.api.repositories.AuthRepository
import tv.dustypig.dustypig.api.repositories.NotificationsRepository
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.search.tmdb_details.TMDBDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.friend_details_settings.FriendDetailsSettingsNav
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private data class FCMAlertData(
    val id: Int,
    val profileId: Int,
    val mediaId: Int?,
    val mediaType: MediaTypes?,
    val notificationType: NotificationTypes,
    val friendshipId: Int?
)

@Singleton
class AlertsManager @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val authManager: AuthManager,
    private val settingsManager: SettingsManager,
    private val authRepository: AuthRepository
) {
    companion object {

        private const val TAG = "AlertsManager"

        const val DATA_ID = "dp.id"
        const val DATA_PROFILE_ID = "dp.pid"

        private const val DATA_MEDIA_ID = "dp.mid"
        private const val DATA_MEDIA_TYPE = "dp.mt"
        private const val DATA_NOTIFICATION_TYPE = "dp.nt"
        private const val DATA_FRIENDSHIP_ID = "dp.fid"

        private const val FIRESTORE_ALERTS_COLLECTION_PATH = "alerts"

        // These are used to move data from the static object to the instance
        private val _mutableUpdateFlow = MutableSharedFlow<String>(replay = 1)
        private val _mutableAlertFlow = MutableSharedFlow<FCMAlertData?>(replay = 1)
        private val _mutableMarkReadFlow = MutableSharedFlow<Int>(replay = 1)
        private val _mutableDeleteFlow = MutableSharedFlow<Int>(replay = 1)
        private val _mutableNavRouteFlow = MutableSharedFlow<String>(replay = 1)
        private val _notificationsFlow = MutableSharedFlow<List<Notification>>(replay = 1)
        private val _updateFCMTokenFlow = MutableSharedFlow<String>(replay = 1)

        fun triggerUpdateFCMToken() {
            _updateFCMTokenFlow.tryEmit(UUID.randomUUID().toString())
        }

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
                    NotificationTypes.OverrideMediaRequested -> getMediaRoute(
                        mediaId!!,
                        mediaType!!
                    )

                    NotificationTypes.OverrideMediaGranted -> getMediaRoute(mediaId!!, mediaType!!)
                    NotificationTypes.OverrideMediaRejected -> getMediaRoute(mediaId!!, mediaType!!)

                    NotificationTypes.FriendshipInvited -> FriendDetailsSettingsNav.getRouteForId(
                        friendshipId!!
                    )

                    NotificationTypes.FriendshipAccepted -> FriendDetailsSettingsNav.getRouteForId(
                        friendshipId!!
                    )
                }
            } catch (ex: Exception) {
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
                    detailedPlaylistId = -1,
                    fromPlaylist = false,
                    playlistUpNextIndex = 0
                )

                MediaTypes.Series -> SeriesDetailsNav.getRoute(mediaId)

                MediaTypes.Episode -> EpisodeDetailsNav.getRoute(
                    parentId = -1,
                    mediaId = mediaId,
                    canPlay = true,
                    source = EpisodeDetailsNav.SOURCE_NOTIFICATION,
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
                    isMovie = true
                )

                MediaTypes.Series -> TMDBDetailsNav.getRoute(
                    mediaId = mediaId,
                    isMovie = false
                )

                else -> null
            }
        }

    }


    val navRouteFlow = _mutableNavRouteFlow.asSharedFlow()
    val notifications = _notificationsFlow.asSharedFlow()

    private val updateFCMTokenFlow = _updateFCMTokenFlow.asSharedFlow()

    private var listenerRegistration: ListenerRegistration? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            _mutableAlertFlow.collect {
                alertTapped(it)
            }
        }

        scope.launch {
            _mutableMarkReadFlow.collect {
                markAsRead(it)
            }
        }

        scope.launch {
            _mutableDeleteFlow.collect {
                delete(it)
            }
        }

        scope.launch {
            _mutableUpdateFlow.collectLatest {
                loadData()
            }
        }

        scope.launch {
            authManager.loginState.collectLatest { loggedIn ->
                listenerRegistration?.remove()
                if (loggedIn == true) {
                    listenerRegistration = Firebase.firestore
                        .collection(FIRESTORE_ALERTS_COLLECTION_PATH)
                        .document(authManager.currentProfileId.toString())
                        .addSnapshotListener { _, err ->
                            if (err != null) {
                                Log.e(TAG, err.localizedMessage ?: "Unknown error", err)
                            } else {
                                Log.d(TAG, "Firestore sent alerts signal")
                                _mutableUpdateFlow.tryEmit(UUID.randomUUID().toString())
                            }
                        }
                }
            }
        }

        scope.launch {
            updateFCMTokenFlow.collect {
                updateFCMToken()
            }
        }
    }


    //Update FCM token on server
    private suspend fun updateFCMToken() {
        try {
            if (authManager.currentProfileId < 1)
                return

            val newFCMToken =
                if (settingsManager.getAllowNotifications())
                    FCMManager.currentToken
                else
                    null

            val newAuthToken = authRepository.updateFCMToken(FCMToken(newFCMToken))
            authManager.setAuthToken(newAuthToken)
        } catch (ex: Exception) {
            ex.logToCrashlytics()
        }
    }


    private suspend fun loadData() {
        if (authManager.currentProfileId < 1) {
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

        if (authManager.currentProfileId < 1)
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
            notificationsRepository.markAsRead(id)
        } catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage ?: "Unknown error", ex)
            ex.logToCrashlytics()
        }
    }

    private suspend fun delete(id: Int) {
        try {
            notificationsRepository.delete(id)
        } catch (ex: Exception) {
            ex.logToCrashlytics()
        }
    }
}