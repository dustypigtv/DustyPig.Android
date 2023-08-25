package tv.dustypig.dustypig.ui.main_app.screens.episode_details

import androidx.lifecycle.ViewModel
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

class EpisodeDetailsViewModel  @Inject constructor(
    private val routeNavigator: RouteNavigator
): ViewModel(), RouteNavigator by routeNavigator {

}