package tv.dustypig.dustypig.ui.main_app

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tv.dustypig.dustypig.ui.main_app.screens.show_more.ShowMoreScreen
import tv.dustypig.dustypig.ui.main_app.screens.show_more.ShowMoreScreenRoute
import tv.dustypig.dustypig.ui.main_app.screens.downloads.DownloadsScreenRoute
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeScreenRoute
import tv.dustypig.dustypig.ui.main_app.screens.search.SearchScreenRoute
import tv.dustypig.dustypig.ui.main_app.screens.settings.SettingsScreenRoute


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav(){

    val navController = rememberNavController()

    val items = mapOf(
        Pair(HomeScreenRoute.route, Icons.Filled.Home),
        Pair(SearchScreenRoute.route, Icons.Filled.Search),
        Pair(DownloadsScreenRoute.route, Icons.Filled.Download),
        Pair(SettingsScreenRoute.route, Icons.Filled.Settings)
    )


    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.value,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.key } == true,
                        onClick = {
                            navController.navigate(screen.key) {
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
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = HomeScreenRoute.route, Modifier.padding(innerPadding)) {

            //Tab screens
            HomeScreenRoute.composable(this, navController)
            SearchScreenRoute.composable(this, navController)
            DownloadsScreenRoute.composable(this, navController)
            SettingsScreenRoute.composable(this, navController)

            // Sub Screens
            ShowMoreScreenRoute.composable(this, navController)

        }
    }
}