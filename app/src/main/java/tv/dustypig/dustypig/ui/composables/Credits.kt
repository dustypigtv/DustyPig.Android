package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.settings_manager.Themes
import tv.dustypig.dustypig.ui.theme.DustyPigTheme


private val spacerHeight = 12.dp


data class CreditsData(
    val genres: List<String> = listOf(),
    val cast: List<String> = listOf(),
    val directors: List<String> = listOf(),
    val producers: List<String> = listOf(),
    val writers: List<String> = listOf(),
    val owner: String = ""
)


@Composable
private fun CreditsRow(header: String, maxHeaderWidth: MutableState<Dp>, items: List<String>) {

    //Draw twice:
    //First, draw the header rows, and get the max width.
    //Then redraw with both fields, setting the header width
    //Yes it sucks. I can't find another way

    if(items.isNotEmpty()) {

        val density = LocalDensity.current
        var measured by remember {mutableStateOf(false)}

        Spacer(modifier = Modifier.height(spacerHeight))
        Row (
            modifier = Modifier
                .padding(12.dp, 0.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if(measured) {
                Text(
                    text = header,
                    modifier = Modifier.width(maxHeaderWidth.value),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Text(
                    text = items.joinToString(", "),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            } else {
                Text(
                    text = header,
                    modifier = Modifier
                        .onGloballyPositioned {
                            val width = with(density) { it.size.width.toDp() }
                            if (width > maxHeaderWidth.value)
                                maxHeaderWidth.value = width
                            measured = true
                        },
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}


@Composable
fun Credits(creditsData: CreditsData) {

    val genresHeader = if(creditsData.genres.count() > 1) stringResource(R.string.genres) else stringResource(R.string.genre)
    val directorsHeader = if(creditsData.directors.count() > 1) stringResource(R.string.directors) else stringResource(R.string.director)
    val producersHeader = if(creditsData.producers.count() > 1) stringResource(R.string.producers) else stringResource(R.string.producer)
    val writersHeader = if(creditsData.writers.count() > 1) stringResource(R.string.writers) else stringResource(R.string.writer)

    Spacer(modifier = Modifier.height(spacerHeight))

    val maxHeaderWidth = remember { mutableStateOf(0.dp) }

    CreditsRow(header = genresHeader, maxHeaderWidth = maxHeaderWidth, items = creditsData.genres)
    CreditsRow(header = stringResource(R.string.cast), maxHeaderWidth = maxHeaderWidth, items = creditsData.cast)
    CreditsRow(header = directorsHeader, maxHeaderWidth = maxHeaderWidth, items = creditsData.directors)
    CreditsRow(header = producersHeader, maxHeaderWidth = maxHeaderWidth, items = creditsData.producers)
    CreditsRow(header = writersHeader, maxHeaderWidth = maxHeaderWidth, items = creditsData.writers)
    if(creditsData.owner.isNotBlank())
        CreditsRow(header = stringResource(R.string.owner), maxHeaderWidth = maxHeaderWidth, items = listOf(creditsData.owner))

    Spacer(modifier = Modifier.height(spacerHeight))
}


// The preview doesn't work all the time
@Preview
@Composable
private fun CreditsPreview() {
    val creditsData = CreditsData(
        genres = listOf("Genre 1", "Genre 2"),
        cast = listOf("Actress 1", "Actor 2", "Actress 3", "Actor 4", "Actress 5", "Actor 6"),
        directors = listOf("Director 1", "Director 2"),
        producers = listOf("Producer"),
        writers = listOf("Writer 1", "Writer 2")
    )

    DustyPigTheme(currentTheme = Themes.Maggies) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Credits(creditsData)
            }
        }
    }
}

























