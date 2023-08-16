package tv.dustypig.dustypig.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Dp.toPXInt() = with(LocalDensity.current) { this@toPXInt.roundToPx() }