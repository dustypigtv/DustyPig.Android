package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator
): ViewModel(), RouteNavigator by routeNavigator  {

    companion object {
        var mediaType = MediaTypes.Movie
        var upNextId = 0

        var detailedMovie: DetailedMovie? = null
        var detailedSeries: DetailedSeries? = null
        var detailedEpisode: DetailedEpisode? = null
        var detailedPlaylist: DetailedPlaylist? = null
    }

    private val _uiState = MutableStateFlow(PlayerUIState())
    val uiState: StateFlow<PlayerUIState> = _uiState.asStateFlow()

    init {

    }

}