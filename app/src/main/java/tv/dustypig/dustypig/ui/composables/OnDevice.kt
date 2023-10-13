package tv.dustypig.dustypig.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import tv.dustypig.dustypig.ui.isTablet

@Composable
fun OnDevice (onPhone: @Composable () -> Unit, onTablet: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    if(configuration.isTablet()) {
        onTablet()
    } else {
        onPhone()
    }
}