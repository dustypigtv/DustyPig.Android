//https://github.com/androidx/media/blob/release/libraries/ui/src/main

package tv.dustypig.dustypig.ui.main_app.screens.player

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import tv.dustypig.dustypig.ui.composables.CastButton
import tv.dustypig.dustypig.ui.composables.CastControls
import tv.dustypig.dustypig.ui.composables.CastSlider
import tv.dustypig.dustypig.ui.composables.ErrorDialog


@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(vm: PlayerViewModel) {
    val uiState by vm.uiState.collectAsState()
    PlayerScreenInternal(uiState = uiState)
}



@OptIn(UnstableApi::class)
@Composable
private fun PlayerScreenInternal(uiState: PlayerUIState) {

    BackHandler(enabled = true) {
        uiState.onPopBackStack()
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




    var showControls by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        if(uiState.isCastPlayer) {

            // Cast Controls
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center
                ){
                    CastControls(castManager = uiState.castManager, sizeMultiple = 2, showBusy = uiState.busy)
                }

                CastSlider(
                    castManager = uiState.castManager,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    displayOnly = false,
                    showTime = true,
                    useTheme = false
                )
            }

        } else {

            // Exoplayer
            val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

            AndroidView(
                factory = { context ->
                    PlayerView(context).also { playerView ->
                        playerView.player = uiState.player
                        playerView.keepScreenOn = true
                        playerView.useController = true

                        playerView.player = uiState.player
                        playerView.keepScreenOn = true
                        playerView.setShowSubtitleButton(true)
                        playerView.setControllerVisibilityListener(
                            PlayerView.ControllerVisibilityListener { visible ->
                                showControls = visible == PlayerView.VISIBLE
                            }
                        )
                        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)

                        // Default is a dark green spinner - fix that
                        try {
                            val progressBar = playerView.findViewById<ProgressBar>(androidx.media3.ui.R.id.exo_buffering)
                            DrawableCompat.setTint(
                                progressBar.indeterminateDrawable,
                                primaryColor
                            )
                        } catch (_: Throwable) {
                        }
                    }
                },
                update = {
                    when (lifecycle) {

                        Lifecycle.Event.ON_STOP -> {
                            it.onPause()
                        }

                        Lifecycle.Event.ON_RESUME -> {
                            it.onResume()
                        }

                        else -> Unit
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }


        // Top Bar
        val delayTime = 250

        //<color name="exo_bottom_bar_background">#b0000000</color>
        val barBackgroundColor = Color(red = 0, green = 0, blue = 0, alpha = 0xb0)

        AnimatedVisibility(
            visible = showControls || uiState.isCastPlayer,
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
                    .background(color = barBackgroundColor),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = uiState.onPopBackStack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

                CastButton(uiState.castManager)
            }
        }



        // Skip Credits
        AnimatedVisibility(
            visible = uiState.currentPositionWithinIntro,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 104.dp)
        ) {
            Button(
                onClick = uiState.onSkipIntro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(text = stringResource(tv.dustypig.dustypig.R.string.skip_intro))
            }
        }

        // Play Next
        AnimatedVisibility(
            visible = uiState.currentPositionWithinCredits,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 104.dp)
        ) {
            Button(
                onClick = uiState.onPlayNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(text = stringResource(tv.dustypig.dustypig.R.string.next))
            }
        }



    }
    
    if (uiState.showErrorDialog) {
        ErrorDialog(
            onDismissRequest = uiState.onPlayNext,
            message = uiState.errorMessage
        )
    }
}
