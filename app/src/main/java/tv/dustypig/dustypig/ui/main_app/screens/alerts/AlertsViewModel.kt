package tv.dustypig.dustypig.ui.main_app.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.repositories.NotificationsRepository
import tv.dustypig.dustypig.global_managers.AlertsManager
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject


@HiltViewModel
class AlertsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    private val alertsManager: AlertsManager,
    private val notificationsRepository: NotificationsRepository
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        AlertsUIState(
            onItemClicked = ::itemClicked,
            onDeleteItem = ::deleteItem,
            onMarkAllRead = ::markAllRead,
            onDeleteAll = ::deleteAll
        )
    )
    val uiState = _uiState.asStateFlow()

    init {

        viewModelScope.launch {
            alertsManager.notifications.collectLatest { list ->
                _uiState.update { state ->
                    state.copy(
                        busy = false,
                        loaded = true,
                        notifications = list,
                        hasUnread = list.count { !it.seen } > 0
                    )
                }
            }
        }
    }


    private fun itemClicked(id: Int) {

        AlertsManager.triggerMarkAsRead(id)

        val notification = _uiState.value.notifications.firstOrNull {
            it.id == id
        } ?: return

        val route = AlertsManager.getNavRoute(
            notificationType = notification.notificationType,
            mediaId = notification.mediaId,
            mediaType = notification.mediaType,
            friendshipId = notification.friendshipId
        ) ?: return

        navigateToRoute(route)
    }

    private fun deleteItem(id: Int) {
        AlertsManager.triggerDelete(id)
    }

    private fun markAllRead() {
        _uiState.update { it.copy(busy = true) }
        viewModelScope.launch {
            notificationsRepository.markAllAsRead()
            AlertsManager.triggerUpdate()
        }
    }

    private fun deleteAll() {
        _uiState.update { it.copy(busy = true) }
        viewModelScope.launch {
            notificationsRepository.deleteAll()
            AlertsManager.triggerUpdate()
        }
    }
}