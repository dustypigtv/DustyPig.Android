package tv.dustypig.dustypig.ui.theme

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(

    primary = Color.White,
    onPrimary = Color.Black,

    secondary = Color.Red,
    onSecondary = Color.Green,

    tertiary = Color.Green,
    onTertiary = Color.Red,

    primaryContainer = Color.White,
    onPrimaryContainer = Color.Black,

    secondaryContainer = DarkGray,
    onSecondaryContainer = Color.White,

    tertiaryContainer = DialogGray,
    onTertiaryContainer = Color.White,

    background = Color.Black,
    onBackground = Color.White,

    surface = Color.Black,
    onSurface = Color.White,

    surfaceVariant = Color.Black,
    onSurfaceVariant = Color.Gray,

    outline = Color.White,

    error = Color.Red,
)


@Composable
fun DustyPigTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ThemePreview() {

    DustyPigTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {


        }
    }

}
