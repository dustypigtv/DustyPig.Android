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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import tv.dustypig.dustypig.api.models.BasicTMDB
import tv.dustypig.dustypig.api.models.TMDBMediaTypes
import tv.dustypig.dustypig.global_managers.ArtworkCache
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.search.tmdb_details.TMDBDetailsNav


private val wdp = 100.dp
private val hdp = 150.dp


@Composable
fun BasicTMDBView(
    basicTMDB: BasicTMDB,
    routeNavigator: RouteNavigator,
    clicked: (() -> Unit?)? = null
) {

    var showAlternate by remember { mutableStateOf(false) }

    val modifier = remember {
        Modifier
            .background(color = Color.DarkGray, shape = RoundedCornerShape(4.dp))
            .size(wdp, hdp)
            .clip(RoundedCornerShape(4.dp))
            .clickable {
                if (clicked != null) {
                    clicked()
                }
                ArtworkCache.add(basicTMDB)
                routeNavigator.navigateToRoute(
                    TMDBDetailsNav.getRoute(
                        mediaId = basicTMDB.tmdbId,
                        isMovie = basicTMDB.mediaType == TMDBMediaTypes.Movie
                    )
                )
            }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(hdp)
    ) {

        if (showAlternate) {
            Box(
                modifier = modifier.align(Alignment.Center)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = basicTMDB.title,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true,
                    color = Color.White
                )
            }
        } else {
             AsyncImage(
                model = ImageRequest
                    .Builder(LocalContext.current)
                    .data(basicTMDB.artworkUrl)
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




















