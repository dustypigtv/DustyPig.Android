package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.global_managers.media_cache_manager.MediaCacheManager
import tv.dustypig.dustypig.nav.MyRouteNavigator
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.playlist_details.PlaylistDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav
import java.util.UUID


@Composable
fun BasicMediaView(
    basicMedia: BasicMedia,
    routeNavigator: RouteNavigator? = null,
    enabled: Boolean = true,
    navigateOnClick: Boolean = true,
    clicked: ((Int) -> Unit)? = null
) {

    fun onClicked() {

        if(clicked != null)
            clicked(basicMedia.id)

        if(routeNavigator == null)
            return

        if(!navigateOnClick)
            return

        val cachedId = MediaCacheManager.add(basicMedia)

        when (basicMedia.mediaType) {
            MediaTypes.Movie -> {
                routeNavigator.navigateToRoute(
                    route = MovieDetailsNav.getRoute(
                        mediaId = basicMedia.id,
                        basicCacheId = cachedId,
                        detailedPlaylistCacheId = UUID.randomUUID().toString(),
                        fromPlaylist = false,
                        playlistUpNextIndex = 0
                    )
                )
            }

            MediaTypes.Series -> {
                routeNavigator.navigateToRoute(
                    route = SeriesDetailsNav.getRoute(
                        mediaId = basicMedia.id,
                        basicCacheId = cachedId
                    )
                )
            }

            MediaTypes.Playlist -> {
                routeNavigator.navigateToRoute(
                    route = PlaylistDetailsNav.getRoute(
                        mediaId = basicMedia.id,
                        cacheId = cachedId
                    )
                )
            }

            else -> { }
        }
    }

    val wdp = 100.dp
    val hdp = 150.dp

    val clickableModifier = if(enabled) Modifier.clickable { onClicked() } else Modifier

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(hdp)
    ) {

        AsyncImage(
            model = basicMedia.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = clickableModifier
                .background(color = Color.DarkGray, shape = RoundedCornerShape(4.dp))
                .align(Alignment.Center)
                .size(wdp, hdp)
                .clip(RoundedCornerShape(4.dp)),
            error = painterResource(id = R.drawable.error_tall)
        )
    }
}



@Preview
@Composable
private fun BasicMediaViewPreview() {
    PreviewBase {
        BasicMediaView(
            basicMedia = BasicMedia(
                id = 1,
                mediaType = MediaTypes.Movie,
                artworkUrl = "https://s3.dustypig.tv/demo-media/Movies/Big%20Buck%20Bunny%20%282008%29.jpg",
                backdropUrl = "https://s3.dustypig.tv/demo-media/Movies/Big%20Buck%20Bunny%20%282008%29.backdrop.jpg",
                title = "Big Buck Bunny (2008)"
            ),
            routeNavigator = MyRouteNavigator()
        )
    }
}
