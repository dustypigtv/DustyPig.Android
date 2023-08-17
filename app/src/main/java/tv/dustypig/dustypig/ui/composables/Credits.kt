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

@Composable
fun Credits(genres: List<String>, cast: List<String>, directors: List<String>, producers: List<String>, writers: List<String>, owner: String) {

    //Cast: = 40
    //Owner: = 53
    //Genres: = 58
    //Writers: = 59
    //Directors: = 74
    //Producers: = 82
    //Add 12 to final size

    val headerWidth =
        if(producers.isNotEmpty()) 94.dp
        else if(directors.isNotEmpty()) 86.dp
        else if(writers.isNotEmpty()) 71.dp
        else if(genres.isNotEmpty()) 70.dp
        else if(owner.isNotBlank()) 65.dp
        else 52.dp

    val genresHeader = if(genres.count() > 1) "Genres:" else "Genre:"
    val directorsHeader = if(directors.count() > 1) "Directors:" else "Director:"
    val producersHeader = if(producers.count() > 1) "Producers:" else "Producer:"
    val writersHeader = if(writers.count() > 1) "Writers:" else "Writer:"

    Spacer(modifier = Modifier.height(spacerHeight))

    CreditsRow(header = genresHeader, headerWidth = headerWidth, items = genres)
    CreditsRow(header = "Cast:", headerWidth = headerWidth, items = cast)
    CreditsRow(header = directorsHeader, headerWidth = headerWidth, items = directors)
    CreditsRow(header = producersHeader, headerWidth = headerWidth, items = producers)
    CreditsRow(header = writersHeader, headerWidth = headerWidth, items = writers)
    CreditsRow(header = "Owner:", headerWidth = headerWidth, items = owner)

    Spacer(modifier = Modifier.height(spacerHeight))
}