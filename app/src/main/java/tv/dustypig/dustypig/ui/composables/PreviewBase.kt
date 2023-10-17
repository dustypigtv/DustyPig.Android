package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tv.dustypig.dustypig.global_managers.settings_manager.Themes
import tv.dustypig.dustypig.ui.theme.DustyPigTheme

@Composable
fun PreviewBase(preview: @Composable () -> Unit) {
    DustyPigTheme(currentTheme = Themes.Maggies) {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            preview()
        }
    }
}