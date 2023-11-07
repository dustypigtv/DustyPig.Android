package tv.dustypig.dustypig.ui.main_app.screens.player

import android.view.View
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Forward30
import androidx.compose.material.icons.outlined.Replay10
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.media3.ui.R
import tv.dustypig.dustypig.ui.composables.CastButton
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.hideSystemUi
import tv.dustypig.dustypig.ui.showSystemUi


private val disabledWhite = Color.White.copy(alpha = 0.38f)

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

    val delayTime = 250
    var showExtendedControls by remember { mutableStateOf(false) }



    //val castTimeText: String = "0:00 • 0:00",

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {

        if(uiState.isCastPlayer) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center
                ){
                    PlaybackButton(
                        onClick = {
                            if (uiState.castPosition <= 10) {
                                if(uiState.castHasPrevious)
                                    uiState.castManager.playPrevious()
                                else
                                    uiState.castManager.seekTo(0f)
                            } else {
                                uiState.castManager.seekTo(0f)
                            }
                        },
                        enabled = true,
                        imageVector = Icons.Filled.SkipPrevious
                    )

                    PlaybackButton(
                        onClick = { uiState.castManager.seekBy(-10f) },
                        enabled = true,
                        imageVector = Icons.Outlined.Replay10
                    )

                    Box {
                        PlaybackButton(
                            onClick = uiState.castManager::togglePlayPause,
                            enabled = true,
                            imageVector =
                                if (uiState.castPaused)
                                    Icons.Filled.PlayCircle
                                else
                                    Icons.Filled.PauseCircle
                        )
                        if(uiState.castBuffering || uiState.busy) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    PlaybackButton(
                        onClick = { uiState.castManager.seekBy(30f) },
                        enabled = true,
                        imageVector = Icons.Outlined.Forward30
                    )

                    PlaybackButton(
                        onClick = uiState.castManager::playNext,
                        enabled = uiState.castHasNext,
                        imageVector = Icons.Filled.SkipNext
                    )


                }

                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {

                    var sliderRawValue by remember { mutableFloatStateOf(uiState.castPosition) }
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val isDragged by interactionSource.collectIsDraggedAsState()
                    val isInteracting = isPressed || isDragged

                    val sliderValue = if(isInteracting)
                        sliderRawValue
                    else
                        uiState.castPosition

                    if(!isInteracting)
                        sliderRawValue = sliderValue

                    Slider(
                        modifier = Modifier
                            .padding(12.dp, 0.dp)
                            .fillMaxWidth(),
                        valueRange = 0f..uiState.castDuration,
                        value = sliderValue,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTickColor = disabledWhite
                        ),
                        onValueChange = { sliderRawValue = it },
                        onValueChangeFinished = { uiState.castManager.seekTo(sliderValue) },
                        interactionSource = interactionSource
                    )

                    Row (
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = formatTime(sliderValue) + " • " + formatTime(uiState.castDuration),
                            color = Color.White
                        )
                    }
                }
            }

        } else {

            val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

            AndroidView(
                factory = { context ->
                    PlayerView(context).also { playerView ->
                        playerView.player = uiState.player
                        playerView.keepScreenOn = true
                        playerView.setShowSubtitleButton(true)
                        playerView.setControllerVisibilityListener(
                            PlayerView.ControllerVisibilityListener { visible ->
                                showExtendedControls = visible == PlayerView.VISIBLE
                            }
                        )
                        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                        playerView.setFullscreenButtonClickListener {
                            if (it)
                                context.hideSystemUi()
                            else
                                context.showSystemUi()
                        }

                        try {
                            //Start in full screen but also make sure correct mode is set
                            playerView.findViewById<View>(R.id.exo_fullscreen).performClick()
                        } catch (_: Throwable) {
                        }

                        // Default is a dark green spinner - fix that
                        try {
                            val progressBar =
                                playerView.findViewById<ProgressBar>(R.id.exo_buffering)
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
        }





        AnimatedVisibility(
            visible = showExtendedControls || uiState.isCastPlayer,
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
                    onClick = uiState.onPopBackStack
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

                CastButton(uiState.castManager)
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
                onClick = uiState.onSkipIntro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(text = stringResource(tv.dustypig.dustypig.R.string.skip_intro))
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


@Composable
private fun PlaybackButton(
    onClick: () -> Unit,
    enabled: Boolean,
    imageVector: ImageVector
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(color = Color.Transparent)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = false,
                    radius = 40.dp
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint =
                if(enabled)
                    Color.White
                else
                    disabledWhite
        )

    }
}


private fun formatTime(seconds: Float): String {
    var s = seconds.toInt()
    val h = s / 3600
    val m = (s % 3600) / 60
    s %= 60
    return if(h > 0)
        "%1d:%02d:%02d".format(h, m, s)
    else
        "%02d:%02d".format(m, s)
}
































