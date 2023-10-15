package tv.dustypig.dustypig.ui.main_app

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.NotificationsManager
import tv.dustypig.dustypig.global_managers.fcm_manager.FCMManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav
import javax.inject.Inject



@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val notificationsManager: NotificationsManager
): ViewModel() {

    companion object {
        private const val TAG = "AppNavViewModel"

        //Logic: Static flow that can be updated from MainActivity
        //Flow updates the main scaffold, which will then call navigate
        private val _mutableDeepLinkFlow = MutableSharedFlow<String>(replay = 1)

        fun queueDeepLink(deepLink: String) = _mutableDeepLinkFlow.tryEmit(deepLink)
    }

    val deepLinkFlow = _mutableDeepLinkFlow.asSharedFlow()

    private val _unseenNotifications = MutableStateFlow(false)
    val unseenNotifications = _unseenNotifications.asStateFlow()

    val snackbarHostState = SnackbarHostState()

    init {

        viewModelScope.launch {
            FCMManager.inAppAlerts.collectLatest {

                var text = it.title
                if(it.message.isNotBlank())
                    text += "\n\n" + it.message + "\n"

                val result = if(it.deepLink.isNullOrBlank()) {
                    snackbarHostState.showSnackbar(
                        message = text,
                        duration = SnackbarDuration.Long
                    )
                } else {
                    snackbarHostState.showSnackbar(
                        message = text,
                        actionLabel = "View",
                        duration = SnackbarDuration.Long
                    )
                }

                if (result == SnackbarResult.ActionPerformed) {
                    queueDeepLink(it.deepLink!!)
                }

                //Snackbar notifications are seen
                notificationsManager.markAsRead(it.id)
            }
        }

        viewModelScope.launch {
            notificationsManager.notifications.collectLatest { list ->
                val hasUnseen = list.any { !it.seen }
                _unseenNotifications.update { hasUnseen }
            }
        }


    }

    fun navToDeepLink(navHostController: NavHostController, route: String) {
        try {

            if(route.isBlank())
                return

            val parts = route.split('/')
            val type = parts[0]
            val id = parts[1].toInt()

            when(type) {
                "movie" -> { navHostController.navigate(MovieDetailsNav.getRouteForId(id)) }
                "series" -> { navHostController.navigate(SeriesDetailsNav.getRouteForId(id)) }
                "friendship" -> { }
                "requests" -> { }
            }


        } catch(ex: Exception) {
            Log.e(TAG, "navToDeepLink", ex)
            ex.logToCrashlytics()
        }

    }



}
