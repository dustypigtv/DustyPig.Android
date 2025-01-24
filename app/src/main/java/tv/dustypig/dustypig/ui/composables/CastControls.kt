package tv.dustypig.dustypig.ui.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Forward30
import androidx.compose.material.icons.outlined.Replay10
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastPlaybackStatus
import tv.dustypig.dustypig.ui.theme.DisabledWhite

@OptIn(UnstableApi::class)
@Composable
fun CastControls(castManager: CastManager, sizeMultiple: Int, showBusy: Boolean) {

    val castState by castManager.castState.collectAsState()

    CastPlaybackButton(
        onClick = {
            if (castState.position <= 10_000) {
                if (castState.hasPrevious)
                    castManager.playPrevious()
                else
                    castManager.seekTo(0)
            } else {
                castManager.seekTo(0)
            }
        },
        enabled = true,
        imageVector = Icons.Filled.SkipPrevious,
        sizeMultiple = sizeMultiple
    )

    CastPlaybackButton(
        onClick = { castManager.seekBy(-10_000) },
        enabled = true,
        imageVector = Icons.Outlined.Replay10,
        sizeMultiple = sizeMultiple
    )

    Box {
        CastPlaybackButton(
            onClick = castManager::togglePlayPause,
            enabled = true,
            imageVector =
            if (castState.playbackStatus == CastPlaybackStatus.Paused ||
                castState.playbackStatus == CastPlaybackStatus.Stopped
            )
                Icons.Filled.PlayCircle
            else
                Icons.Filled.PauseCircle,
            sizeMultiple = sizeMultiple
        )
        if (castState.playbackStatus == CastPlaybackStatus.Buffering || showBusy) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(40.dp * (sizeMultiple / 2))
                    .align(Alignment.Center)
            )
        }
    }

    CastPlaybackButton(
        onClick = { castManager.seekBy(30_000) },
        enabled = true,
        imageVector = Icons.Outlined.Forward30,
        sizeMultiple = sizeMultiple
    )

    CastPlaybackButton(
        onClick = castManager::playNext,
        enabled = castState.hasNext,
        imageVector = Icons.Filled.SkipNext,
        sizeMultiple = sizeMultiple
    )
}


@Composable
private fun CastPlaybackButton(
    onClick: () -> Unit,
    enabled: Boolean,
    imageVector: ImageVector,
    sizeMultiple: Int
) {
    Box(
        modifier = Modifier
            .size(40.dp * sizeMultiple)
            .clip(CircleShape)
            .background(color = Color.Transparent)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = false,
                    radius = 40.dp * (sizeMultiple / 2)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(24.dp * sizeMultiple),
            imageVector = imageVector,
            contentDescription = null,
            tint =
            if (enabled)
                Color.White
            else
                DisabledWhite
        )
    }
}
