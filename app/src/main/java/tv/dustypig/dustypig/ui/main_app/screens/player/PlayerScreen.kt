package tv.dustypig.dustypig.ui.main_app.screens.player

import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.media3.ui.R
import androidx.mediarouter.app.MediaRouteActionProvider
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase


@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(vm: PlayerViewModel) {
    val uiState by vm.uiState.collectAsState()
    PlayerScreenInternal(
        popBackStack = vm::popBackStack,
        skipIntro = vm::skipIntro,
        playNext = vm::playNext,
        uiState = uiState
    )

}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerScreenInternal(
    popBackStack: () -> Unit,
    skipIntro: () -> Unit,
    playNext: () -> Unit,
    uiState: PlayerUIState
) {

    BackHandler(enabled = true) {
        popBackStack()
    }

    var lifecycle by remember {
        mutableStateOf(Lifecycle.Event.ON_CREATE)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val delayTime = 250
    var showExtendedControls by remember { mutableStateOf(false) }


    val context = LocalContext.current
    val mediaRouteActionProvider = remember {
        MediaRouteActionProvider(context).also {
            it.routeSelector = MediaRouteSelector
                .Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                .build()
        }
    }


    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).also { playerView ->
                    playerView.player = uiState.player
                    playerView.setShowSubtitleButton(uiState.currentItemHasSubtitles)
                    playerView.setControllerVisibilityListener(
                        PlayerView.ControllerVisibilityListener { visible ->
                            showExtendedControls = visible == PlayerView.VISIBLE
                        }
                    )

                    playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)

                    // Default is a dark green spinner - fix that
                    try {
                        val progressBar = playerView.findViewById<ProgressBar>(R.id.exo_buffering)
                        DrawableCompat.setTint(
                            progressBar.indeterminateDrawable,
                            android.graphics.Color.WHITE
                        )
                    } catch (_: Throwable) {
                    }
                }
            },
            update = {
                when (lifecycle) {
                    Lifecycle.Event.ON_PAUSE -> {
                        it.onPause()
                        it.player?.pause()
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        it.onResume()
                    }

                    else -> Unit
                }
            },
            modifier = Modifier
                .fillMaxSize()
        )



        AnimatedVisibility(
            visible = showExtendedControls,
            enter = expandVertically(
                animationSpec = tween(
                    durationMillis = delayTime,
                )
            ),
            exit = shrinkVertically(
                animationSpec = tween(
                    durationMillis = delayTime,
                )
            )
        ) {

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Transparent),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = popBackStack
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Text(
                    text = uiState.currentItemTitle ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp, 0.dp)
                )


                AndroidView(factory = {
                   mediaRouteActionProvider.onCreateActionView().also {
                        it.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                })
            }
        }

        AnimatedVisibility(
            visible = uiState.currentPositionWithinIntro,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 104.dp)
        ) {
            Button(
                onClick = skipIntro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Skip Intro")
            }
        }

        AnimatedVisibility(
            visible = uiState.currentPositionWithinCredits,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 104.dp)
        ) {
            Button(
                onClick = playNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Next")
            }
        }



    }
    
    if (uiState.showErrorDialog) {
        ErrorDialog(
            onDismissRequest = playNext,
            message = uiState.errorMessage
        )
    }
}


@Preview
@Composable
private fun PlayerScreenPreview() {
    val uiState = PlayerUIState (

    )
    PreviewBase {
        PlayerScreenInternal(
            popBackStack = { },
            skipIntro = { },
            playNext = { },
            uiState = uiState
        )
    }
}