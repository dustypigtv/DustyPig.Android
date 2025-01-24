package tv.dustypig.dustypig.ui.main_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastConnectionState
import tv.dustypig.dustypig.global_managers.cast_manager.CastPlaybackStatus
import tv.dustypig.dustypig.ui.composables.CastDialog
import tv.dustypig.dustypig.ui.composables.CastSlider
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.alerts.AlertsNav
import tv.dustypig.dustypig.ui.main_app.screens.downloads.DownloadsNav
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeNav
import tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title.ManageParentalControlsForTitleNav
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.person_details.PersonDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import tv.dustypig.dustypig.ui.main_app.screens.playlist_details.PlaylistDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.search.SearchNav
import tv.dustypig.dustypig.ui.main_app.screens.search.tmdb_details.TMDBDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.SettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings.AccountSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.download_settings.DownloadSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.FriendsSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.friend_details_settings.FriendDetailsSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.notification_settings.NotificationSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.playback_settings.PlaybackSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.ProfilesSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile.EditProfileNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.switch_profiles.SwitchProfilesNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.theme_settings.ThemeSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.show_more.ShowMoreNav
import kotlin.OptIn
import androidx.annotation.OptIn as CastOptIn


private data class RootScreenMap(
    val name: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val notifications: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@CastOptIn(UnstableApi::class)
@Composable
fun AppNav(vm: AppNavViewModel = hiltViewModel()) {

    val navController = rememberNavController()
    val unseenNotifications by vm.notificationCount.collectAsState(null)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val playerVisible by PlayerStateManager.playerScreenVisible.collectAsState()
    val castState by vm.castManager.castState.collectAsState()

    //Prevent flicker
    var castArtworkUrl by remember {
        mutableStateOf(castState.artworkUrl)
    }
    if (castArtworkUrl != castState.artworkUrl) {
        castArtworkUrl = castState.artworkUrl
    }

    //Because there are multiple paths to the same route, using
    //  selected = currentDestination?.hierarchy?.any { it.route == screen.key } == true
    //doesn't work well here. So instead just remember the new 'root tab route' when it changes
    var curRootRoute by remember {
        mutableStateOf(HomeNav.route)
    }


    val items = listOf(
        RootScreenMap(
            name = stringResource(id = R.string.home),
            route = HomeNav.route,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        RootScreenMap(
            name = stringResource(id = R.string.search),
            route = SearchNav.route,
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search
        ),
        RootScreenMap(
            name = stringResource(id = R.string.downloads),
            route = DownloadsNav.route,
            selectedIcon = Icons.Filled.Download,
            unselectedIcon = Icons.Outlined.Download
        ),
        RootScreenMap(
            name = stringResource(R.string.alerts),
            route = AlertsNav.route,
            selectedIcon = Icons.Filled.Notifications,
            unselectedIcon = Icons.Outlined.Notifications,
            notifications = true
        ),
        RootScreenMap(
            name = stringResource(id = R.string.settings),
            route = SettingsNav.route,
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )


    Scaffold(
        bottomBar = {

            if (!playerVisible) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    if (castState.castPossible() &&
                        castState.castConnectionState == CastConnectionState.Connected &&
                        castState.playbackStatus != CastPlaybackStatus.Stopped
                    ) {

                        var showDialog by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp)
                                )
                                .clickable {
                                    showDialog = true
                                }
                        ) {
                            AsyncImage(
                                model = castArtworkUrl,
                                contentDescription = null,
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier.background(color = Color.DarkGray),
                                alignment = Alignment.CenterStart
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = castState.title ?: "",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Playing on ${castState.selectedRoute?.name}",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = LocalContentColor.current.copy(alpha = 0.5f),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    IconButton(
                                        onClick = { vm.castManager.togglePlayPause() }
                                    ) {
                                        TintedIcon(
                                            imageVector =
                                            if (castState.playbackStatus == CastPlaybackStatus.Paused)
                                                Icons.Filled.PlayArrow
                                            else
                                                Icons.Filled.Pause
                                        )
                                    }
                                    IconButton(onClick = { vm.castManager.disconnect() }) {
                                        TintedIcon(imageVector = Icons.Filled.Close)
                                    }
                                }
                                CastSlider(
                                    castManager = vm.castManager,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, end = 4.dp),
                                    displayOnly = true,
                                    useTheme = true
                                )
                            }

                        }

                        if (showDialog) {
                            CastDialog(
                                closeDialog = { showDialog = false },
                                castManager = vm.castManager
                            )
                        }

                    }

                    NavigationBar {
                        items.forEach { screen ->
                            NavigationBarItem(
                                alwaysShowLabel = false,
                                label = {
                                    Text(
                                        text = screen.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (screen.notifications && unseenNotifications != null) {
                                                Badge(
                                                    containerColor = Color.Red
                                                ) {
                                                    Text(unseenNotifications!!)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (screen.route == curRootRoute) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = null
                                        )

                                    }
                                },
                                selected = screen.route == curRootRoute,
                                onClick = {

                                    if (curRootRoute == screen.route && curRootRoute != currentDestination?.route) {
                                        navController.popBackStack()
                                    } else {
                                        curRootRoute = screen.route

                                        navController.navigate(screen.route) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            // on the back stack as users select items
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            // Avoid multiple copies of the same destination when
                                            // re-selecting the same item
                                            launchSingleTop = true

                                            // Restore state when re-selecting a previously selected item
                                            restoreState = true
                                        }
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
            }

        }
    ) { innerPadding ->
        NavHost(navController, startDestination = HomeNav.route, Modifier.padding(innerPadding)) {

            //Tab screens
            HomeNav.composable(this, navController)
            SearchNav.composable(this, navController)
            DownloadsNav.composable(this, navController)
            AlertsNav.composable(this, navController)
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
            PlayerNav.composable(this, navController)
            PersonDetailsNav.composable(this, navController)

            //Settings
            ThemeSettingsNav.composable(this, navController)
            AccountSettingsNav.composable(this, navController)
            FriendsSettingsNav.composable(this, navController)
            FriendDetailsSettingsNav.composable(this, navController)
            ProfilesSettingsNav.composable(this, navController)
            EditProfileNav.composable(this, navController)
            PlaybackSettingsNav.composable(this, navController)
            NotificationSettingsNav.composable(this, navController)
            DownloadSettingsNav.composable(this, navController)
            SwitchProfilesNav.composable(this, navController)
        }
    }

    //The nav controller doesn't exist yet when the viewModel is created,
    //So have to do this here
    LaunchedEffect(true) {
        vm.alertsManager.navRouteFlow.collectLatest {
            try {
                navController.navigate(it)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}