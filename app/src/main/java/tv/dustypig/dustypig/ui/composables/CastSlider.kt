package tv.dustypig.dustypig.ui.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.ui.theme.DisabledWhite


@OptIn(UnstableApi::class)
@Composable
fun CastSlider(
    castManager: CastManager,
    modifier: Modifier = Modifier,
    showTime: Boolean = true,
    displayOnly: Boolean = false,
    useTheme: Boolean = true
) {

    val castState by castManager.castState.collectAsState()

    val activeTrackColor = if(useTheme) MaterialTheme.colorScheme.primary else Color.White
    val inactiveTrackColor = if(useTheme) MaterialTheme.colorScheme.surfaceVariant else DisabledWhite

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            if (displayOnly) {
                LinearProgressIndicator(
                    progress = { castState.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = activeTrackColor,
                    trackColor = inactiveTrackColor,
                )
            } else {

                val duration by remember(castState.duration) { mutableFloatStateOf(castState.duration.toFloat()) }
                var draggedPosition by remember { mutableFloatStateOf(castState.position.toFloat()) }
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val isDragged by interactionSource.collectIsDraggedAsState()
                val isInteracting = isPressed || isDragged

                val displayValue by remember(isInteracting, draggedPosition, castState.position) {
                    derivedStateOf {
                        if (isInteracting) {
                            draggedPosition
                        } else {
                            castState.position.toFloat()
                        }
                    }
                }

                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    valueRange = 0f..duration,
                    value = displayValue,
                    colors = SliderDefaults.colors(
                        thumbColor = activeTrackColor,
                        activeTrackColor = activeTrackColor,
                        inactiveTrackColor = inactiveTrackColor
                    ),
                    onValueChange = { draggedPosition = it },
                    onValueChangeFinished = { castManager.seekTo(displayValue.toLong()) },
                    interactionSource = interactionSource
                )

                if (showTime) {

                    fun formatTime(milliseconds: Float): String {
                        var s = milliseconds.toLong() / 1000
                        val h = s / 3600
                        val m = (s % 3600) / 60
                        s %= 60
                        return if (h > 0)
                            "%1d:%02d:%02d".format(h, m, s)
                        else
                            "%02d:%02d".format(m, s)
                    }

                    val posText by remember(displayValue) { mutableStateOf(formatTime(displayValue)) }
                    val durText by remember(duration) { mutableStateOf(formatTime(duration)) }

                    Text(
                        modifier = Modifier.padding(8.dp, 0.dp),
                        text = "$posText  •  $durText",
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        color =
                        if (useTheme)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            Color.White
                    )

                }

            }
        }
    }

}