package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicPerson
import tv.dustypig.dustypig.global_managers.ArtworkCache
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.person_details.PersonDetailsNav


private val wdp = 100.dp
private val hdp = 150.dp


@Composable
fun BasicPersonView(
    basicPerson: BasicPerson,
    routeNavigator: RouteNavigator,
    clicked: (() -> Unit?)? = null
) {
    Box(
        modifier = Modifier.padding(0.dp, 12.dp)
    ) {
        Column (
            modifier = Modifier
                .align(Alignment.Center)
                .width(100.dp)
        ) {
            AsyncImage(
                model = ImageRequest
                    .Builder(LocalContext.current)
                    .data(basicPerson.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.grey_profile),
                error = painterResource(R.drawable.grey_profile),
                modifier = Modifier
                    .clickable {
                        if(clicked != null) {
                            clicked()
                        }
                        ArtworkCache.add(basicPerson)
                        routeNavigator.navigateToRoute(
                            PersonDetailsNav.getRoute(basicPerson.tmdbId)
                        )
                    }
                    .background(color = Color.DarkGray, shape = RoundedCornerShape(4.dp))
                    .size(wdp, hdp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = basicPerson.name,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                maxLines = 2,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}