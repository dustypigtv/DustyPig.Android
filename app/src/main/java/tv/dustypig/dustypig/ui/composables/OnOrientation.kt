package tv.dustypig.dustypig.ui.composables

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration


@Composable
fun OnOrientation (onPortrait: @Composable () -> Unit, onLandscape: @Composable () -> Unit, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        onLandscape()
    } else {
        onPortrait()
    }
}