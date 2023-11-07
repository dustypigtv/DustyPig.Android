package tv.dustypig.dustypig.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import tv.dustypig.dustypig.global_managers.settings_manager.Themes


private val maggiesColorScheme = darkColorScheme(
    primary = MaggiePink,
    onPrimary = Color.White,
    background = Color.Black,
    onBackground = MaggieYellow,
    secondaryContainer = MaggieLightPink,
    tertiaryContainer = MaggieDimPink
)


private val dustyPigColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = DustyPigDarkGray,
    secondaryContainer = DustyPigGray,
    tertiaryContainer = DustyPigGray
)


private fun accentColorScheme(
    primary: Color
): ColorScheme {

    //background is black + 5% primary
    val background = Color(primary.red * 0.05f, primary.green * 0.05f, primary.blue * 0.05f, 1f)

    //foreground is white

    //secondaryContainer primary -> 75% closer to white
    val r2 = primary.red + ((1f - primary.red) * 0.75f)
    val g2 = primary.green + ((1f - primary.green) * 0.75f)
    val b2 = primary.blue + ((1f - primary.blue) * 0.75f)
    val secondaryContainer = Color(r2, g2, b2, 1f)

    //tertiaryContainer is primary -> 50% closer to black, with 50% alpha
    val tertiaryContainer = primary.copy(0.5f, primary.red * 0.5f, primary.green * 0.5f, primary.blue * 0.5f)

    return darkColorScheme(
        primary = primary,
        onPrimary = Color.White,
        background = background,
        onBackground = Color.White,
        surface = background,
        secondaryContainer = secondaryContainer,
        tertiaryContainer = tertiaryContainer
    )
}


@Composable
fun DustyPigTheme(
    currentTheme: Themes,
    content: @Composable () -> Unit
) {
    val colorScheme = when(currentTheme) {
        Themes.Maggies -> maggiesColorScheme
        Themes.DustyPig -> dustyPigColorScheme
        Themes.LB -> accentColorScheme(LBPrimary)
        Themes.Red -> accentColorScheme(NetflixRed)
        Themes.HuluGreen -> accentColorScheme(HuluGreen)
        Themes.DisneyBlue -> accentColorScheme(DisneyBlue)
        Themes.BurntOrange -> accentColorScheme(BurntOrange)
        else -> darkColorScheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {

        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

//        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
//        val playerVisible by PlayerStateManager.playerScreenVisible.collectAsState()
//        if (playerVisible) {
//            insetsController.apply {
//                hide(WindowInsetsCompat.Type.systemBars())
//                //systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//            }
//        } else {
//            insetsController.apply {
//                show(WindowInsetsCompat.Type.systemBars())
//                //systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
//            }
//        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

