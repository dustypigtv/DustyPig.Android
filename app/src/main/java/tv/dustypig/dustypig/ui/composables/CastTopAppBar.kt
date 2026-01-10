package tv.dustypig.dustypig.ui.composables

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastTopAppBar(
    onClick: () -> Unit,
    text: String,
    castManager: CastManager?
) {

    val context = LocalContext.current
    val mediaRouteButton = try {
        val button = MediaRouteButton(context)
        CastButtonFactory.setUpMediaRouteButton(context, button)
        button
    } catch (_: Exception) {
        null
    }

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
            if(mediaRouteButton != null) {
                AndroidView(
                    factory = { _ -> mediaRouteButton },
                )
            }
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
