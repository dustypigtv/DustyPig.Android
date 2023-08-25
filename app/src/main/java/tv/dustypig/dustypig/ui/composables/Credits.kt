package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


private val spacerHeight = 12.dp


@Composable
private fun HeaderText(header: String, headerWidth: Dp) {
    Text(
        text = header,
        modifier = Modifier.width(headerWidth),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ItemsText(items: String) {
    Text(
        text = items,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ItemsText(items: List<String>) {
    ItemsText(items.joinToString(", "))
}



@Composable
private fun CreditsRow(header: String, headerWidth: Dp, items: String) {

    if(items.isNotBlank()) {
        Spacer(modifier = Modifier.height(spacerHeight))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            HeaderText(header = header, headerWidth)
            ItemsText(items = items)
        }
    }
}


@Composable
private fun CreditsRow(header: String, headerWidth: Dp, items: List<String>) {

    if(items.isNotEmpty()) {
        Spacer(modifier = Modifier.height(spacerHeight))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            HeaderText(header = header, headerWidth)
            ItemsText(items = items)
        }
    }
}


data class CreditsData(
    val genres: List<String> = listOf(),
    val cast: List<String> = listOf(),
    val directors: List<String> = listOf(),
    val producers: List<String> = listOf(),
    val writers: List<String> = listOf(),
    val owner: String = ""
)

@Composable
fun Credits(creditsData: CreditsData) {

    //Cast: = 40
    //Owner: = 53
    //Genres: = 58
    //Writers: = 59
    //Directors: = 74
    //Producers: = 82
    //Add 12 to final size

    val headerWidth =
        if(creditsData.producers.isNotEmpty()) 94.dp
        else if(creditsData.directors.isNotEmpty()) 86.dp
        else if(creditsData.writers.isNotEmpty()) 71.dp
        else if(creditsData.genres.isNotEmpty()) 70.dp
        else if(creditsData.owner.isNotBlank()) 65.dp
        else 52.dp

    val genresHeader = if(creditsData.genres.count() > 1) "Genres:" else "Genre:"
    val directorsHeader = if(creditsData.directors.count() > 1) "Directors:" else "Director:"
    val producersHeader = if(creditsData.producers.count() > 1) "Producers:" else "Producer:"
    val writersHeader = if(creditsData.writers.count() > 1) "Writers:" else "Writer:"

    Spacer(modifier = Modifier.height(spacerHeight))

    CreditsRow(header = genresHeader, headerWidth = headerWidth, items = creditsData.genres)
    CreditsRow(header = "Cast:", headerWidth = headerWidth, items = creditsData.cast)
    CreditsRow(header = directorsHeader, headerWidth = headerWidth, items = creditsData.directors)
    CreditsRow(header = producersHeader, headerWidth = headerWidth, items = creditsData.producers)
    CreditsRow(header = writersHeader, headerWidth = headerWidth, items = creditsData.writers)
    CreditsRow(header = "Owner:", headerWidth = headerWidth, items = creditsData.owner)

    Spacer(modifier = Modifier.height(spacerHeight))
}