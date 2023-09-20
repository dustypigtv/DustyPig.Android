package tv.dustypig.dustypig.ui.main_app

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.NotificationsManager
import tv.dustypig.dustypig.global_managers.fcm_manager.FCMManager
import javax.inject.Inject

@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val notificationsManager: NotificationsManager
): ViewModel() {

    companion object {

        //Logic: Static flow that can be updated from MainActivity
        //Flow updates the main scaffold, which will then call navigate
        private val _navFlow = MutableStateFlow<String?>(null)
        val navFlow = _navFlow.asStateFlow()

        fun queueNavRoute(deepLink: String) = _navFlow.tryEmit(deepLink)
    }


    val snackbarHostState = SnackbarHostState()

    init {
        viewModelScope.launch {
            FCMManager.inAppAlerts.collectLatest {

                if(it != null) {
                    val result = snackbarHostState.showSnackbar(
                        message = it.title,
                        actionLabel = "View",
                        duration = SnackbarDuration.Long
                    )

                    if(result == SnackbarResult.ActionPerformed) {
                        queueNavRoute(it.deepLink!!)
                    }

                    //Snackbar notifications are seen
                    notificationsManager.markAsRead(it.id)
                }
            }
        }
    }

}
