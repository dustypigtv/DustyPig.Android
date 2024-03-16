package tv.dustypig.dustypig.ui.main_app.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.HomeScreenList
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.global_managers.NetworkManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.show_more.ShowMoreNav
import java.util.Calendar
import java.util.Date
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val mediaRepository: MediaRepository,
    private val  networkManager: NetworkManager,
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        HomeUIState(
            onRefresh = ::refresh,
            onShowMoreClicked = ::navToShowMore
        )
    )
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    private val _timer = Timer()
    private var _timerBusy = false

    companion object {
        private var _nextTimerTick: Date = Calendar.getInstance().time
        fun triggerUpdate() {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.SECOND, -1)
            _nextTimerTick = calendar.time
        }

        private fun waitMinute() {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, 1)
            _nextTimerTick = calendar.time
        }
    }

    init {
        _timer.schedule(
            delay = 0,
            period = 1000
        ){
            loadData()
        }
    }

    private fun loadData() {

        if(_uiState.value.sections.isEmpty())
            triggerUpdate()

        _uiState.update {
            it.copy(
                hasNetworkConnection = networkManager.isConnected(),
                isRefreshing = if (it.sections.isEmpty()) true else it.isRefreshing
            )
        }

        if(Calendar.getInstance().time < _nextTimerTick)
            return

        if(_timerBusy)
            return

        _timerBusy = true

        viewModelScope.launch {
            try {
                val data = mediaRepository.homeScreen()
                val sections = data.sections ?: listOf()
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        sections = sections
                    )
                }
            } catch (ex: Exception) {
                ex.logToCrashlytics()

                //Only show error message if manually refreshing (or first load)
                if(_uiState.value.isRefreshing) {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            showErrorDialog = true,
                            errorMessage = ex.localizedMessage
                        )
                    }
                }
            }

            waitMinute()
            _timerBusy = false
        }
    }

    private fun refresh() {
        _uiState.update {
            it.copy(isRefreshing = true)
        }
        triggerUpdate()
    }

    private fun navToShowMore(hsl: HomeScreenList) {
        navigateToRoute(ShowMoreNav.getRoute(hsl.listId, hsl.title))
    }
}
