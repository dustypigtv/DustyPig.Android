package tv.dustypig.dustypig.ui.main_app.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.NotificationsManager
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject


@HiltViewModel
class AlertsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    private val notificationsManager: NotificationsManager,
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(AlertsUIState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            notificationsManager.notifications.collectLatest { list ->
                _uiState.update {
                    it.copy(
                        busy = false,
                        notifications = list
                    )
                }
            }
        }
    }


    fun itemClicked(id: Int) {

        val notification = _uiState.value.notifications.firstOrNull {
            it.id == id
        } ?: return

        if(!notification.seen) {
            notificationsManager.markAsRead(id)
        }

        if(!notification.deepLink.isNullOrEmpty()) {
            val route = notificationsManager.getNavRoute(notification.deepLink)
            navigateToRoute(route)
        }
    }

    fun deleteItem(id: Int) {
        _uiState.update {
            it.copy(busy = true)
        }
        notificationsManager.delete(id)
    }
}