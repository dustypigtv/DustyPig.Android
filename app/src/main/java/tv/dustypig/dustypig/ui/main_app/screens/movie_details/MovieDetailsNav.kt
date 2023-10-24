package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object MovieDetailsNav : NavRoute<MovieDetailsViewModel> {

    const val KEY_MEDIA_ID = "KEY_MEDIA_ID"
    const val KEY_BASIC_CACHE_ID = "KEY_BASIC_CACHE_ID"
    const val KEY_DETAILED_PLAYLIST_CACHE_ID = "KEY_DETAILED_PLAYLIST_CACHE_ID"
    const val KEY_FROM_PLAYLIST_ID = "KEY_FROM_PLAYLIST_ID"
    const val KEY_PLAYLIST_UPNEXT_INDEX_ID = "KEY_PLAYLIST_UPNEXT_INDEX_ID"

    override val route = "movieDetails/{$KEY_MEDIA_ID}/{$KEY_BASIC_CACHE_ID}/{$KEY_DETAILED_PLAYLIST_CACHE_ID}/{$KEY_FROM_PLAYLIST_ID}/{$KEY_PLAYLIST_UPNEXT_INDEX_ID}"

    fun getRoute(
        mediaId: Int,
        basicCacheId: String,
        detailedPlaylistCacheId: String,
        fromPlaylist: Boolean,
        playlistUpNextIndex: Int
    ): String = route
        .replace("{$KEY_MEDIA_ID}", "$mediaId")
        .replace("{$KEY_BASIC_CACHE_ID}", basicCacheId)
        .replace("{$KEY_DETAILED_PLAYLIST_CACHE_ID}", detailedPlaylistCacheId)
        .replace("{$KEY_FROM_PLAYLIST_ID}", "$fromPlaylist")
        .replace("{$KEY_PLAYLIST_UPNEXT_INDEX_ID}", "$playlistUpNextIndex")

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_MEDIA_ID) { type = NavType.IntType },
        navArgument(KEY_BASIC_CACHE_ID) { type = NavType.StringType },
        navArgument(KEY_DETAILED_PLAYLIST_CACHE_ID) { type = NavType.StringType },
        navArgument(KEY_FROM_PLAYLIST_ID) { type = NavType.BoolType },
        navArgument(KEY_PLAYLIST_UPNEXT_INDEX_ID) { type = NavType.IntType }
    )

    @Composable
    override fun viewModel(): MovieDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: MovieDetailsViewModel) = MovieDetailsScreen(vm = viewModel)

}