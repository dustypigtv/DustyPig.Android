package tv.dustypig.dustypig.ui.theme

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
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

            //Default color: surface
            //Content color: onSurface
            TopAppBar(
                title = {
                    Text(text = "Toolbar text")
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Favorite, contentDescription = null)
                    }
                }
            )


            //Default color: surface
            //Content color: onSurface
            Card {
                Text(text = "Card", modifier = Modifier.padding(8.dp))
            }

            //Default color: surface
            //Content color: onBackground
            Surface {
                Text(text = "Surface", modifier = Modifier.padding(8.dp))
            }

            //Default color: primaryContainer
            //Content color: onPrimaryContainer
            FloatingActionButton(onClick = { }) {
                Text(text = "FAB", textAlign = TextAlign.Center)
            }

            //Background color: primary
            //Content color: onPrimary
            Button(onClick = { }) {
                Text(text="Button")
            }

            //Background color: transparent
            //Content color: primary
            TextButton(onClick = { }) {
                Text(text = "Text Button")
            }

            //Background color: surface
            //Content color: primary
            //Border color: outline
            OutlinedButton(onClick = { }) {
                Text(text = "Outlined button")
            }


            Surface {

                //Default color: onBackground
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Filled.Visibility, "")
                }
            }


            //Default color (Checked): secondaryVariant/secondaryVariantAlpha
            Switch(checked = true, onCheckedChange = {})

            //Default color (Unchecked): surface/onSurface
            Switch(checked = false, onCheckedChange = {})

            //Border color: outline
            //Text Color: onSurface
            OutlinedTextField(value = "", onValueChange = {},
                placeholder = {Text("Placeholder")})

            OutlinedTextField(value = "", onValueChange = {},
                label={Text("Label")})

            OutlinedTextField(value = "Text", onValueChange = {}, label={Text("Label")})

            CircularProgressIndicator(modifier = Modifier.size(20.dp))

            //AlertDialog(onDismissRequest = { /*TODO*/ }, confirmButton = { /*TODO*/ }, title={ Text("Hello")}, text = { Text("World") })
        }
    }

}
