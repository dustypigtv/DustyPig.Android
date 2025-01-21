package tv.dustypig.dustypig.ui.main_app.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.AlertsManager
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject


@HiltViewModel
class AlertsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    private val alertsManager: AlertsManager
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        AlertsUIState(
            onItemClicked = ::itemClicked,
            onDeleteItem = ::deleteItem
        )
    )
    val uiState = _uiState.asStateFlow()

    init {

        viewModelScope.launch {
            alertsManager.notifications.collectLatest { list ->
                _uiState.update {
                    it.copy(
                        busy = false,
                        notifications = list
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
}