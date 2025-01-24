package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastTopAppBar(
    onClick: () -> Unit,
    text: String,
    castManager: CastManager?
) {

    TopAppBar(
        title = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onClick) {
                TintedIcon(Icons.AutoMirrored.Filled.ArrowBack)
            }
        },
        actions = {
           CastButton(castManager = castManager)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Preview
@Composable
private fun CastTopAppBarPreview() {
    PreviewBase {

        Scaffold(
            topBar = {
                CastTopAppBar(
                    onClick = { },
                    text = "App Bar",
                    castManager = null
                )
            }
        ) {
            Box(
                modifier = Modifier.padding(it)
            ) {

            }
        }
    }
}
