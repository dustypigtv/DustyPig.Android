package tv.dustypig.dustypig.ui.main_app.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator
): ViewModel(), RouteNavigator by routeNavigator {

}