package tv.dustypig.dustypig.ui.main_app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.downloads.DownloadsNav
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeNav
import tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title.ManageParentalControlsForTitleNav
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.playlist_details.PlaylistDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.search.SearchNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.SettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.show_more.ShowMoreNav
import tv.dustypig.dustypig.ui.main_app.screens.tmdb_details.TMDBDetailsNav
import tv.dustypig.dustypig.ui.theme.AccentColor
import tv.dustypig.dustypig.ui.theme.SnackbarBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav(vm: AppNavViewModel = hiltViewModel()){

    val uiState by vm.navFlow.collectAsState()
    val navController = rememberNavController()


    val items = mapOf(
        Pair(stringResource(R.string.home), Pair(HomeNav.route, Icons.Filled.Home)),
        Pair(stringResource(R.string.search), Pair(SearchNav.route, Icons.Filled.Search)),
        Pair(stringResource(R.string.downloads), Pair(DownloadsNav.route, Icons.Filled.Download)),
        Pair(stringResource(R.string.settings), Pair(SettingsNav.route, Icons.Filled.Settings))
    )



    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = vm.snackbarHostState,
                snackbar = { snackbarData: SnackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = SnackbarBackgroundColor,
                        actionColor = AccentColor,
                        actionContentColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                //Because there are multiple paths to the same route, using
                //  selected = currentDestination?.hierarchy?.any { it.route == screen.key } == true
                //doesn't work well here. So instead just rememebr the new 'root tab route' when it changes
                val curRootRoute = remember {
                    mutableStateOf("")
                }

                fun isSelected(compareRoute: String): Boolean {

                    val newRoute = currentDestination?.route
                    if(newRoute != null) {
                        if(items.any { it.value.first == newRoute }) {
                            curRootRoute.value = newRoute
                        }
                    }

                    return curRootRoute.value == compareRoute
                }

                items.forEach { screen ->
                    NavigationBarItem(
                        label = { Text(text= screen.key) },
                        icon = {
                            Icon(
                                imageVector = screen.value.second,
                                contentDescription = null
                            )
                        },
                        selected = isSelected(screen.value.first),
                        onClick = {
                            navController.navigate(screen.value.first) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true

                                // Restore state when reselecting a previously selected item
                                //restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = HomeNav.route, Modifier.padding(innerPadding)) {

            //Tab screens
            HomeNav.composable(this, navController)
            SearchNav.composable(this, navController)
            DownloadsNav.composable(this, navController)
            SettingsNav.composable(this, navController)

            //Sub screens
            ShowMoreNav.composable(this, navController)
            MovieDetailsNav.composable(this, navController)
            SeriesDetailsNav.composable(this, navController)
            EpisodeDetailsNav.composable(this, navController)
            PlaylistDetailsNav.composable(this, navController)
            TMDBDetailsNav.composable(this, navController)
            AddToPlaylistNav.composable(this, navController)
            ManageParentalControlsForTitleNav.composable(this, navController)
        }

        if(uiState.navFromNotification) {
            vm.doNav(navController, uiState.navRoute)
        }
    }
}