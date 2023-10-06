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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.nav.MyRouteNavigator
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.playlist_details.PlaylistDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav


@Composable
fun BasicMediaView(
    basicMedia: BasicMedia,
    routeNavigator: RouteNavigator,
    enabled: Boolean = true,
    navigateOnClick: Boolean = true,
    clicked: ((Int) -> Unit)? = null
) {

    fun onClicked() {

        if(clicked != null)
            clicked(basicMedia.id)


        if(!navigateOnClick)
            return

        ScreenLoadingInfo.setInfo(
            title = basicMedia.title,
            posterUrl = basicMedia.artworkUrl,
            backdropUrl = basicMedia.backdropUrl ?: ""
        )

        when (basicMedia.mediaType) {
            MediaTypes.Movie -> {
                routeNavigator.navigateToRoute(route = MovieDetailsNav.getRouteForId(id = basicMedia.id))
            }
            MediaTypes.Series -> {
                routeNavigator.navigateToRoute(route = SeriesDetailsNav.getRouteForId(id = basicMedia.id))
            }
            MediaTypes.Playlist -> {
                routeNavigator.navigateToRoute(route = PlaylistDetailsNav.getRouteForId(id = basicMedia.id))
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
            model = ImageRequest
                .Builder(LocalContext.current)
                .data(basicMedia.artworkUrl)
                .crossfade(true)
                .build(),
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
