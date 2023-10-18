package tv.dustypig.dustypig.ui.main_app

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.NotificationsManager
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import javax.inject.Inject



@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val notificationsManager: NotificationsManager,
    private val app: Application
): ViewModel() {

    companion object {

        //Logic: Static flow that can be updated from MainActivity
        //Flow updates the main scaffold, which will then call navigate
        private val _mutableDeepLinkFlow = MutableStateFlow("")

        fun queueDeepLink(deepLink: String) = _mutableDeepLinkFlow.update {
            deepLink
        }
    }

    val deepLinkFlow = _mutableDeepLinkFlow.asStateFlow()

    private val _unseenNotifications = MutableStateFlow(false)
    val unseenNotifications = _unseenNotifications.asStateFlow()

    init {
        var playerIsVisible = false
        viewModelScope.launch {
            PlayerStateManager.playerScreenVisible.collectLatest {
                playerIsVisible = it
            }

        }
        viewModelScope.launch {
            notificationsManager.notifications.collectLatest { list ->
                val hasUnseen = list.any { !it.seen }
                _unseenNotifications.update { hasUnseen }
                if(hasUnseen && !playerIsVisible) {
                    val mp = MediaPlayer.create(app, R.raw.oink)
                    mp.start()
                }
            }
        }
    }


    fun navToDeepLink(deepLink: String, navController: NavController) {
        val route = notificationsManager.getNavRoute(deepLink)
        navController.navigate(route)
    }
}
























