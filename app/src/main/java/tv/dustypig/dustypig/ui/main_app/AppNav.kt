package tv.dustypig.dustypig.ui.main_app

import android.accounts.Account
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import tv.dustypig.dustypig.ui.main_app.screens.settings.theme_settings.ThemeSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.home.show_more.ShowMoreNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings.AccountSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.tmdb_details.TMDBDetailsNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav(vm: AppNavViewModel = hiltViewModel()){

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val items = mapOf(
        Pair(stringResource(R.string.home), Pair(HomeNav.route, Icons.Filled.Home)),
        Pair(stringResource(R.string.search), Pair(SearchNav.route, Icons.Filled.Search)),
        Pair(stringResource(R.string.downloads), Pair(DownloadsNav.route, Icons.Filled.Download)),
        Pair(stringResource(R.string.settings), Pair(SettingsNav.route, Icons.Filled.Settings))
    )



    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = vm.snackbarHostState
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
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            //SecondaryContainer
                        )
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

            //Settings
            ThemeSettingsNav.composable(this, navController)
            AccountSettingsNav.composable(this, navController)

        }

        scope.launch {
            vm.navFlow.collectLatest {
                vm.doNav(navController, it)
            }
        }
    }
}