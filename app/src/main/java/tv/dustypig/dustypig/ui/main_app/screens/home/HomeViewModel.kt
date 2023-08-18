package tv.dustypig.dustypig.ui.main_app.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.ThePig
import tv.dustypig.dustypig.api.models.HomeScreenList
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.show_more.ShowMoreNav
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(HomeUIState())
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    private val _timer = Timer()

    init {
        _timer.schedule(
            delay = 0,
            period = 60 * 1000
        ){
            loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val data = ThePig.Api.Media.homeScreen()
                val sections = data.sections ?: listOf()
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        sections = sections
                    )
                }
            } catch (ex: Exception) {

                //Only show error message if manually refreshing (or first load)
                if(_uiState.value.isRefreshing) {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            showError = true,
                            errorMessage = ex.localizedMessage ?: "Unknown Error"
                        )
                    }
                }
            }
        }
    }

    fun onRefresh() {
        _uiState.update {
            it.copy(isRefreshing = true)
        }
        loadData()
    }

    fun onShowMoreClicked(hsl: HomeScreenList) {
        ThePig.showMoreData = hsl
        navigateToRoute(ShowMoreNav.route)
    }
}
