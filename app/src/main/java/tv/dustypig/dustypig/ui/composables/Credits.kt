package tv.dustypig.dustypig.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.CreditRoles
import tv.dustypig.dustypig.api.models.Genre
import tv.dustypig.dustypig.api.models.GenrePair
import tv.dustypig.dustypig.api.models.BasicPerson
import tv.dustypig.dustypig.global_managers.media_cache_manager.MediaCacheManager


private val spacerHeight = 24.dp


data class CreditsData(
    val genres: List<GenrePair> = listOf(),
    val genreNav: (genrePair: GenrePair) -> Unit = { },
    val castAndCrew: List<BasicPerson> = listOf(),
    val personNav: (tmdbId: Int, cacheId: String) -> Unit = { i: Int, s: String -> },
    val owner: String = ""
)



@Composable
private fun CreditsRow(creditsData: CreditsData, role: CreditRoles, singleHeader: String, pluralHeader: String) {

    val peopleInRole = remember {
        creditsData.castAndCrew.filter {
            it.role == role
        }.sortedBy {
            it.order
        }.toMutableList()
    }

    if(peopleInRole.isNotEmpty()) {
        val txt by remember {
            mutableStateOf(if(peopleInRole.size > 1) pluralHeader else singleHeader)
        }
        Text(
            modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 6.dp),
            text = txt,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(12.dp, 0.dp),
            state = rememberLazyListState()
        ) {
            items(peopleInRole) { person ->
                Column(
                    modifier = Modifier
                        .width(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AsyncImage(
                        model = person.avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        placeholder = debugPlaceholder(R.drawable.grey_profile),
                        error = painterResource(id = R.drawable.grey_profile),
                        modifier = Modifier
                            .width(100.dp)
                            .height(150.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                val cacheId = MediaCacheManager.add(
                                    person.name,
                                    posterUrl = person.avatarUrl ?: "",
                                    backdropUrl = null
                                )
                                creditsData.personNav(person.tmdbId, cacheId)
                            }

                    )
                    Text(
                        modifier = Modifier.width(100.dp),
                        text = person.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(spacerHeight))
    }
}


@Composable
fun Credits(creditsData: CreditsData) {

    Spacer(modifier = Modifier.height(spacerHeight))

    if(creditsData.genres.isNotEmpty()) {
       LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(12.dp, 0.dp),
            state = rememberLazyListState()
        ) {
            items(creditsData.genres) {genrePair ->
                Button(onClick = { creditsData.genreNav(genrePair) }) {
                    Text(text = genrePair.text)
                }
            }
        }
    }

    if(creditsData.castAndCrew.isNotEmpty()){

        Spacer(modifier = Modifier.height(spacerHeight))

        CreditsRow(
            creditsData = creditsData,
            role = CreditRoles.Cast,
            singleHeader = stringResource(R.string.cast),
            pluralHeader = stringResource(R.string.cast)
        )

        CreditsRow(
            creditsData = creditsData,
            role = CreditRoles.Director,
            singleHeader = stringResource(R.string.director),
            pluralHeader = stringResource(R.string.directors)
        )

        CreditsRow(
            creditsData = creditsData,
            role = CreditRoles.ExecutiveProducer,
            singleHeader = stringResource(R.string.executive_producer),
            pluralHeader = stringResource(R.string.executive_producers)
        )


        CreditsRow(
            creditsData = creditsData,
            role = CreditRoles.Producer,
            singleHeader = stringResource(R.string.producer),
            pluralHeader = stringResource(R.string.producers)
        )

        CreditsRow(
            creditsData = creditsData,
            role = CreditRoles.Writer,
            singleHeader = stringResource(R.string.writer),
            pluralHeader = stringResource(R.string.writers)
        )
    }

    if(creditsData.owner.isNotBlank()) {
        Row (
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = stringResource(id = R.string.owner),
                color = MaterialTheme.colorScheme.primary
            )

            Text(text = creditsData.owner)
        }
    }

    Spacer(modifier = Modifier.height(spacerHeight))

}


// The preview doesn't work all the time
@Preview
@Composable
private fun CreditsPreview() {
    val creditsData = CreditsData(
        genres = listOf(
            GenrePair.fromGenre(Genre.Action),
            GenrePair.fromGenre(Genre.Adventure)
        ),
        castAndCrew = listOf(
            BasicPerson(
                tmdbId = 1,
                name = "Actress 1",
                initials = "A1",
                avatarUrl = "https://s3.dustypig.tv/user-art/profile/green.png",
                order = 1,
                role = CreditRoles.Cast
            ),
            BasicPerson(
                tmdbId = 2,
                name = "Actor 2",
                initials = "A2",
                avatarUrl = null,
                order = 2,
                role = CreditRoles.Cast
            ),
            BasicPerson(
                tmdbId = 3,
                name = "Actress 3",
                initials = "A3",
                avatarUrl = "https://s3.dustypig.tv/user-art/profile/red.png",
                order = 3,
                role = CreditRoles.Cast
            ),
            BasicPerson(
                tmdbId = 4,
                name = "Actor 4",
                initials = "A4",
                avatarUrl = "https://s3.dustypig.tv/user-art/profile/gold.png",
                order = 4,
                role = CreditRoles.Cast
            ),
            BasicPerson(
                tmdbId = 5,
                name = "Actress 5",
                initials = "A5",
                avatarUrl = "https://s3.dustypig.tv/user-art/profile/grey.png",
                order = 5,
                role = CreditRoles.Cast
            ),
            BasicPerson(
                tmdbId = 6,
                name = "Actor 6",
                initials = "A6",
                avatarUrl = "https://s3.dustypig.tv/user-art/profile/green.png",
                order = 6,
                role = CreditRoles.Cast
            ),
            BasicPerson(
                tmdbId = 7,
                name = "Director 7",
                initials = "D7",
                avatarUrl = "https://s3.dustypig.tv/user-art/profile/blue.png",
                order = 1,
                role = CreditRoles.Director
            ),
            BasicPerson(
                tmdbId = 8,
                name = "Director 8",
                initials = "A8",
                avatarUrl = "https://s3.dustypig.tv/user-art/profile/red.png",
                order = 2,
                role = CreditRoles.Director
            ),
            BasicPerson(
                tmdbId = 9,
                name = "Producer 9",
                initials = "P9",
                avatarUrl = "https://s3.dustypig.tv/user-art/profile/gold.png",
                order = 1,
                role = CreditRoles.Producer
            ),
            BasicPerson(
                tmdbId = 10,
                name = "Writer 10",
                initials = "W1",
                avatarUrl = "https://s3.dustypig.tv/user-art/profile/grey.png",
                order = 1,
                role = CreditRoles.Writer
            )
        ),
        owner = "Jason"
    )

    PreviewBase {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Credits(creditsData)
        }
    }
}






@Composable
private fun debugPlaceholder(@DrawableRes debugPreview: Int) =
    if (LocalInspectionMode.current) {
        painterResource(id = debugPreview)
    } else {
        null
    }


















