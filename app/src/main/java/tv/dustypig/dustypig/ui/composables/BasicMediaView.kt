package tv.dustypig.dustypig.ui.composables

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BasicMediaView(
    basicMedia: BasicMedia,
    routeNavigator: RouteNavigator,
    clicked: ((Int) -> Unit)? = null
) {
    fun onClicked() {

        if(clicked != null) {
            clicked(basicMedia.id)
            return
        }

        when (basicMedia.mediaType) {
            MediaTypes.Movie -> {
                routeNavigator.navigateToRoute(MovieDetailsNav.getRouteForId(basicMedia.id))
            }
            MediaTypes.Series -> {
                routeNavigator.navigateToRoute(SeriesDetailsNav.getRouteForId(basicMedia.id))
            }
            MediaTypes.Playlist -> {

            }

            else -> { }
        }
    }

    val wdp = 100.dp
    val hdp = 150.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(hdp)
    ) {
        GlideImage(
            model = basicMedia.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.Center)
                .size(wdp, hdp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onClicked() }
        ) {
            it
                .placeholder(R.drawable.placeholder_tall)
                .error(R.drawable.error_tall)
        }
    }
}