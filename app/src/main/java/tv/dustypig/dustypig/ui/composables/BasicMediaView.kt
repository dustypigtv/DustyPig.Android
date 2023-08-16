package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.ThePig
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.movie_details.MovieDetailsNav


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BasicMediaView(
    basicMedia: BasicMedia,
    routeNavigator: RouteNavigator,
    modifier:Modifier = Modifier
) {
    fun onClicked() {
        ThePig.selectedBasicMedia = basicMedia
        when (basicMedia.mediaType) {
            MediaTypes.Movie -> {
                //routeNavigator.navigateToRoute(MovieDetailsNav.getRouteForId(basicMedia.id))
                routeNavigator.navigateToRoute(MovieDetailsNav.route)
            }
            MediaTypes.Series -> {

            }
            MediaTypes.Playlist -> {

            }

            else -> {}
        }
    }

    val wdp = 100.dp
    val hdp = 150.dp

    Box(
        modifier = modifier
            .size(wdp, hdp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        GlideImage(
            model = basicMedia.artworkUrl,
            contentDescription = null,
            modifier = modifier
                .size(wdp, hdp)
                .clickable { onClicked() }
        ) {
            it
                .placeholder(R.drawable.placeholder_tall)
                .error(R.drawable.error_tall)
        }
    }
}