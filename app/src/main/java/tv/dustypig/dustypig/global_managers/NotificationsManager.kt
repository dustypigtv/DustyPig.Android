package tv.dustypig.dustypig.global_managers

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.Notification
import tv.dustypig.dustypig.api.repositories.NotificationsRepository
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.search.tmdb_details.TMDBDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.friend_details_settings.FriendDetailsSettingsNav
import java.util.Calendar
import java.util.Date
import java.util.Timer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.schedule


@Singleton
class NotificationsManager @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val authManager: AuthManager
) {
    companion object {

        private const val TAG = "NotificationsManager"

        private var _nextTimerTick: Date = Calendar.getInstance().time

        fun triggerUpdate() {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.SECOND, -2)
            _nextTimerTick = calendar.time
        }

        private fun waitMinute() {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, 1)
            _nextTimerTick = calendar.time
        }
    }

    private val _notificationsFlow = MutableSharedFlow<List<Notification>>(replay = 1)
    val notifications = _notificationsFlow.asSharedFlow()

    private val _timer = Timer()
    private var _timerBusy = false

    private var loggedIn = false

    init {
        _timer.schedule(
            delay = 0,
            period = 1000
        ){
            loadData()
        }

        CoroutineScope(Dispatchers.IO).launch {
            authManager.loginState.collectLatest {
                loggedIn = it == AuthManager.LOGIN_STATE_LOGGED_IN
            }
        }
    }

    private fun logError(ex: Exception) {
        ex.printStackTrace()
        ex.logToCrashlytics()
    }

    private fun loadData() {
        if(_timerBusy || Calendar.getInstance().time < _nextTimerTick)
            return

        if(!loggedIn)
            return

        _timerBusy = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lst = notificationsRepository.list()
                _notificationsFlow.tryEmit(lst)
            } catch (ex: Exception) {
                logError(ex = ex)
            }

            waitMinute()
            _timerBusy = false
        }
    }

    fun markAsRead(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationsRepository.markAsRead(id)
                triggerUpdate()
            } catch (ex: Exception) {
                logError(ex = ex)
            }
        }
    }

    fun delete(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationsRepository.delete(id)
                triggerUpdate()
            } catch (ex: Exception) {
                logError(ex = ex)
            }
        }
    }

    fun getNavRoute(deepLink: String): String {
        try {

            val parts = deepLink.split('/')
            val type = parts[0]
            val linkId = parts.last().toInt()

            when(type) {
                "movie", "series" -> {
                    return getMediaRoute(type, linkId)
                }

                "requests" -> {
                    val subType = parts[1]
                    return TMDBDetailsNav.getRoute(mediaId = linkId, cacheId = "", subType == "movie")
                }

                "friendship" -> {
                    return FriendDetailsSettingsNav.getRouteForId(linkId)
                }

            }


        } catch(ex: Exception) {
            Log.e(TAG, "navToDeepLink", ex)
            ex.logToCrashlytics()
        }

        return ""
    }

    private fun getMediaRoute(type: String, id: Int): String {
        when(type) {
            "movie" -> {
                return MovieDetailsNav.getRoute(
                    mediaId = id,
                    basicCacheId = "",
                    detailedPlaylistCacheId = "",
                    fromPlaylist = false,
                    playlistUpNextIndex = 0
                )
            }

            "series" -> {
                return SeriesDetailsNav.getRoute(
                    mediaId = id,
                    basicCacheId = ""
                )
            }
        }

        return ""
    }


}