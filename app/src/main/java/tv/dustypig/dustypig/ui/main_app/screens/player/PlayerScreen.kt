//https://github.com/androidx/media/blob/release/libraries/ui/src/main

package tv.dustypig.dustypig.ui.main_app.screens.player

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Rational
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toRect
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import tv.dustypig.dustypig.ui.composables.CastButton
import tv.dustypig.dustypig.ui.composables.CastControls
import tv.dustypig.dustypig.ui.composables.CastSlider
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.findActivity
import android.graphics.drawable.Icon as AndroidIcon

private const val ACTION_BROADCAST_CONTROL = "broadcast_control"
private const val EXTRA_CONTROL_TYPE = "control_type"
private const val EXTRA_CONTROL_PLAY = 1
private const val EXTRA_CONTROL_PAUSE = 2
private const val REQUEST_PLAY = 5
private const val REQUEST_PAUSE = 6

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(vm: PlayerViewModel) {
    val uiState by vm.uiState.collectAsState()
    PlayerScreenInternal(uiState = uiState)
}


@SuppressLint("PrivateResource")
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


    PipListenerPreAPI12(shouldEnterPipMode = uiState.shouldEnterPictureInPicture)


    var showControls by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        if (uiState.isCastPlayer) {

            // Cast Controls
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CastControls(
                        castManager = uiState.castManager,
                        sizeMultiple = 2,
                        showBusy = uiState.busy
                    )
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

            var isInPipMode by remember { mutableStateOf(false) }

            var exoModifier = Modifier.fillMaxSize()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val context = LocalContext.current

                // create modifier that adds pip to video player
                exoModifier = exoModifier.onGloballyPositioned { layoutCoordinates ->
                    val builder = PictureInPictureParams.Builder()
                    if (
                        uiState.shouldEnterPictureInPicture
                        && uiState.player != null
                        && uiState.player.videoSize != VideoSize.UNKNOWN
                        ) {
                        // set source rect hint, aspect ratio
                        val sourceRect = layoutCoordinates.boundsInWindow().toAndroidRectF().toRect()
                        builder.setSourceRectHint(sourceRect)
                        val aspectRatio = Rational(uiState.player.videoSize.width, uiState.player.videoSize.height)
                        builder.setAspectRatio(aspectRatio)
                    }

                    val playPauseAction = if (
                        uiState.shouldEnterPictureInPicture
                        && uiState.player != null
                        && uiState.player.isPlaying
                        ) {
                        RemoteAction(
                            AndroidIcon.createWithResource(context, androidx.media3.ui.R.drawable.exo_icon_pause),
                            context.getString(androidx.media3.ui.R.string.exo_controls_pause_description),
                            context.getString(androidx.media3.ui.R.string.exo_controls_pause_description),
                            PendingIntent.getBroadcast(
                                context,
                                REQUEST_PAUSE,
                                Intent(ACTION_BROADCAST_CONTROL)
                                    .setPackage(context.packageName)
                                    .putExtra(EXTRA_CONTROL_TYPE, EXTRA_CONTROL_PAUSE),
                                PendingIntent.FLAG_IMMUTABLE,
                            ),
                        )
                    } else {
                        RemoteAction(
                            AndroidIcon.createWithResource(context, androidx.media3.ui.R.drawable.exo_icon_play),
                            context.getString(androidx.media3.ui.R.string.exo_controls_play_description),
                            context.getString(androidx.media3.ui.R.string.exo_controls_play_description),
                            PendingIntent.getBroadcast(
                                context,
                                REQUEST_PLAY,
                                Intent(ACTION_BROADCAST_CONTROL)
                                    .setPackage(context.packageName)
                                    .putExtra(EXTRA_CONTROL_TYPE, EXTRA_CONTROL_PLAY),
                                PendingIntent.FLAG_IMMUTABLE,
                            ),
                        )
                    }

                    builder.setActions(
                        listOf(playPauseAction),
                    )


                    // Add autoEnterEnabled for versions S and up
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        builder.setAutoEnterEnabled(uiState.shouldEnterPictureInPicture)
                    }
                    context.findActivity().setPictureInPictureParams(builder.build())
                }

                isInPipMode = isInPipMode()
            }

            AndroidView(
                factory = { context ->
                    PlayerView(context).also { playerView ->
                        playerView.player = uiState.player
                        playerView.keepScreenOn = true
                        playerView.useController = true
                        playerView.setShowSubtitleButton(true)
                        playerView.setControllerVisibilityListener(
                            PlayerView.ControllerVisibilityListener { visible ->
                                showControls = visible == PlayerView.VISIBLE
                            }
                        )
                        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)

                        // Default is a dark green spinner - fix that
                        try {
                            val progressBar =
                                playerView.findViewById<ProgressBar>(androidx.media3.ui.R.id.exo_buffering)
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

                    it.useController = !isInPipMode

                },
                modifier = exoModifier
            )

            BroadcastReceiver(player = uiState.player)
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = barBackgroundColor),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.padding(12.dp, 0.dp),
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

                Box(modifier = Modifier.padding(12.dp, 0.dp)) {
                    CastButton(uiState.castManager)
                }
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
        if(uiState.errorMessage?.contains("runtime error") != true) {
            ErrorDialog(
                onDismissRequest = uiState.onPlayNext,
                message = uiState.errorMessage
            )
        }
    }
}




/**
 * Uses Disposable Effect to add a pip observer to check when app enters pip mode so UI can be
 * updated
 */

@Composable
fun isInPipMode(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val activity = LocalContext.current.findActivity()
        var pipMode by remember { mutableStateOf(activity.isInPictureInPictureMode) }

        // Uses Disposable Effect to add a pip observer to check when app enters pip mode
        DisposableEffect(activity) {
            val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
                pipMode = info.isInPictureInPictureMode
            }
            activity.addOnPictureInPictureModeChangedListener(
                observer,
            )
            onDispose { activity.removeOnPictureInPictureModeChangedListener(observer) }
        }

        return pipMode
    } else {
        return false
    }
}


/**
 * Uses Disposable Effect to add a listener for onUserLeaveHint - allowing us to add PiP pre
 * Android 12
 */
@Composable
fun PipListenerPreAPI12(shouldEnterPipMode: Boolean) {
    // Using the rememberUpdatedState ensures that the updated version of shouldEnterPipMode is
    // used by the DisposableEffect
    val currentShouldEnterPipMode by rememberUpdatedState(newValue = shouldEnterPipMode)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    ) {
        val activity = LocalContext.current.findActivity()
        DisposableEffect(activity) {
            val onUserLeaveBehavior = {
                if (currentShouldEnterPipMode) {
                    activity.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                }
            }
            activity.addOnUserLeaveHintListener(
                onUserLeaveBehavior,
            )
            onDispose {
                activity.removeOnUserLeaveHintListener(
                    onUserLeaveBehavior,
                )
            }
        }
    }
}


/**
 * Adds a Broadcast Receiver for controls while in pip mode. We are demonstrating how to add custom
 * controls - if you use a MediaSession these controls come with it.
 */
@Composable
fun BroadcastReceiver(player: Player?) {
    if (isInPipMode() && player != null) {
        val context = LocalContext.current
        DisposableEffect(key1 = player, key2 = context) {
            val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if ((intent == null) || (intent.action != ACTION_BROADCAST_CONTROL)) {
                        return
                    }

                    when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                        EXTRA_CONTROL_PAUSE -> try{ player.pause() } catch (_: Throwable) { }
                        EXTRA_CONTROL_PLAY -> try { player.play() } catch (_: Throwable) { }
                    }
                }
            }
            ContextCompat.registerReceiver(
                context,
                broadcastReceiver,
                IntentFilter(ACTION_BROADCAST_CONTROL),
                RECEIVER_NOT_EXPORTED,
            )
            onDispose {
                context.unregisterReceiver(broadcastReceiver)
            }
        }
    }
}