package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.global_managers.ArtworkCache
import tv.dustypig.dustypig.nav.MyRouteNavigator
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.playlist_details.PlaylistDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav


private val wdp = 100.dp
private val hdp = 150.dp


@Composable
fun BasicMediaView(
    basicMedia: BasicMedia,
    routeNavigator: RouteNavigator? = null,
    enabled: Boolean = true,
    navigateOnClick: Boolean = true,
    clicked: ((Int) -> Unit)? = null
) {

    fun onClicked() {

        if (clicked != null)
            clicked(basicMedia.id)

        if (routeNavigator == null)
            return

        if (!navigateOnClick)
            return


        when (basicMedia.mediaType) {
            MediaTypes.Movie -> {
                ArtworkCache.add(basicMedia)
                routeNavigator.navigateToRoute(
                    route = MovieDetailsNav.getRoute(
                        mediaId = basicMedia.id,
                        detailedPlaylistId = -1,
                        fromPlaylist = false,
                        playlistUpNextIndex = 0
                    )
                )
            }

            MediaTypes.Series -> {
                ArtworkCache.add(basicMedia)
                routeNavigator.navigateToRoute(
                    route = SeriesDetailsNav.getRoute(
                        mediaId = basicMedia.id
                    )
                )
            }

            MediaTypes.Playlist -> {
                ArtworkCache.addPlaylist(
                    basicMedia.id,
                    basicMedia.artworkUrl,
                    basicMedia.backdropUrl
                )
                routeNavigator.navigateToRoute(
                    route = PlaylistDetailsNav.getRoute(mediaId = basicMedia.id)
                )
            }

            else -> {}
        }
    }

    val modifier =
        if(enabled) {
            remember {
                Modifier
                    .background(color = Color.DarkGray, shape = RoundedCornerShape(4.dp))
                    .size(wdp, hdp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onClicked() }
            }
        } else {
            remember {
                Modifier
                    .background(color = Color.DarkGray, shape = RoundedCornerShape(4.dp))
                    .size(wdp, hdp)
                    .clip(RoundedCornerShape(4.dp))
            }
        }

    var showAlternate by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(hdp)
    ) {

        if(showAlternate) {
            Box(
                modifier = modifier.align(Alignment.Center)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                    text = basicMedia.title,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true,
                    color = Color.White
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(basicMedia.artworkUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = {
                    showAlternate = true
                },
                modifier = modifier.align(Alignment.Center)
            )
        }
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
