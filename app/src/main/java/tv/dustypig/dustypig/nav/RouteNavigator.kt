package tv.dustypig.dustypig.nav

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo
import javax.inject.Inject

/**
 * Navigator to use when initiating navigation from a ViewModel.
 */
interface RouteNavigator {
    fun onNavigated(state: NavigationState)
    fun navigateUp()
    fun popToRoute(route: String)
    fun popBackStack()
    fun navigateToRoute(route: String)
    fun setScreenLoadingInfo(title: String, posterUrl: String, backdropUrl: String)
    val navigationState: StateFlow<NavigationState>
}

class MyRouteNavigator @Inject constructor(
    val screenLoadingInfo: ScreenLoadingInfo
) : RouteNavigator {

    /**
     * Note that I'm using a single state here, not a list of states. As a result, if you quickly
     * update the state multiple times, the view will only receive and handle the latest state,
     * which is fine for my use case.
     */
    override val navigationState: MutableStateFlow<NavigationState> =
        MutableStateFlow(NavigationState.Idle)

    override fun onNavigated(state: NavigationState) {
        // clear navigation state, if state is the current state:
        navigationState.compareAndSet(state, NavigationState.Idle)
    }

    override fun popToRoute(route: String) = navigate(NavigationState.PopToRoute(route))

    override fun navigateUp() = navigate(NavigationState.NavigateUp())

    override fun popBackStack() = navigate(NavigationState.PopBackStack())

    override fun navigateToRoute(route: String) = navigate(NavigationState.NavigateToRoute(route))

    override fun setScreenLoadingInfo(title: String, posterUrl: String, backdropUrl: String) {
        screenLoadingInfo.title = title
        screenLoadingInfo.posterUrl = posterUrl
        screenLoadingInfo.backdropUrl = backdropUrl
    }


    @VisibleForTesting
    fun navigate(state: NavigationState) {
        navigationState.value = state
    }
}