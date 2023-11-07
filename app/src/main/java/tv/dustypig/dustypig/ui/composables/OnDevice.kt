package tv.dustypig.dustypig.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import tv.dustypig.dustypig.ui.isTablet

@Composable
fun OnDevice (onPhone: @Composable () -> Unit, onTablet: @Composable () -> Unit) {
    if(LocalContext.current.isTablet()) {
        onTablet()
    } else {
        onPhone()
    }
}