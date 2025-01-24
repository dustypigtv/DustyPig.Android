package tv.dustypig.dustypig.ui.main_app

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.AlertsManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import javax.inject.Inject


@HiltViewModel
@OptIn(UnstableApi::class)
class AppNavViewModel @Inject constructor(
    val castManager: CastManager,
    val alertsManager: AlertsManager
) : ViewModel() {

    private val _notificationCountState = MutableStateFlow<String?>(null)
    val notificationCount: StateFlow<String?> = _notificationCountState.asStateFlow()


    init {
        viewModelScope.launch {
            alertsManager.notifications.collectLatest { it ->
                val cnt = it.count { !it.seen }
                val s: String? =
                    if (cnt == 0)
                        null
                    else if (cnt > 99)
                        "99+"
                    else
                        cnt.toString()

                _notificationCountState.tryEmit(s)
            }
        }


    }
}
