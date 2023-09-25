package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.playlist_details.PlaylistDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav


@OptIn(ExperimentalGlideComposeApi::class, ExperimentalComposeUiApi::class)
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
        GlideImage(
            model = basicMedia.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = clickableModifier
                .align(Alignment.Center)
                .size(wdp, hdp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            it
                .placeholder(R.drawable.placeholder_tall)
                .error(R.drawable.error_tall)
        }
    }
}