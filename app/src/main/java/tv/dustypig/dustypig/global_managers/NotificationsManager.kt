package tv.dustypig.dustypig.global_managers

import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.Notification
import tv.dustypig.dustypig.api.repositories.NotificationsRepository
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav
import java.util.Calendar
import java.util.Date
import java.util.Timer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.schedule


@Singleton
@OptIn(DelicateCoroutinesApi::class)
class NotificationsManager @Inject constructor(
    private val notificationsRepository: NotificationsRepository
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


    init {
        _timer.schedule(
            delay = 0,
            period = 1000
        ){
            loadData()
        }
    }

    private fun logError(ex: Exception) {
        ex.printStackTrace()
        ex.logToCrashlytics()
    }

    private fun loadData() {
        if(_timerBusy || Calendar.getInstance().time < _nextTimerTick)
            return

        _timerBusy = true

        GlobalScope.launch {
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
        GlobalScope.launch {
            try {
                notificationsRepository.markAsRead(id)
                triggerUpdate()
            } catch (ex: Exception) {
                logError(ex = ex)
            }
        }
    }

    fun delete(id: Int) {
        GlobalScope.launch {
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
            val linkId = parts[1].toInt()

            when(type) {
                "movie" -> {
                    return MovieDetailsNav.getRoute(mediaId = linkId)
                }

                "series" -> {
                    return SeriesDetailsNav.getRoute(mediaId = linkId)
                }

                "friendship" -> { }
                "requests" -> { }
            }


        } catch(ex: Exception) {
            Log.e(TAG, "navToDeepLink", ex)
            ex.logToCrashlytics()
        }

        return ""
    }


//    fun navToDeepLink(deepLink: String, routeNavigator: RouteNavigator) {
//        try {
//
//            val parts = deepLink.split('/')
//            val type = parts[0]
//            val linkId = parts[1].toInt()
//
//            when(type) {
//                "movie" -> {
//                    routeNavigator.navigateToRoute(
//                        MovieDetailsNav.getRoute(mediaId = linkId)
//                    )
//                }
//
//                "series" ->
//                {
//                    routeNavigator.navigateToRoute(
//                        SeriesDetailsNav.getRoute(mediaId = linkId)
//                    )
//                }
//
//                "friendship" -> { }
//                "requests" -> { }
//            }
//
//
//        } catch(ex: Exception) {
//            Log.e(TAG, "navToDeepLink", ex)
//            ex.logToCrashlytics()
//        }
//
//    }

}