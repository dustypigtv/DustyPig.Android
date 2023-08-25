package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

class PlayerViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    //private val savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator  {

    private val _uiState = MutableStateFlow(PlayerUIState())
    val uiState: StateFlow<PlayerUIState> = _uiState.asStateFlow()

}