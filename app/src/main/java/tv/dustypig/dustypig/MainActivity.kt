package tv.dustypig.dustypig

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.AndroidEntryPoint
import tv.dustypig.dustypig.ui.auth_flow.AuthNav
import tv.dustypig.dustypig.ui.main_app.AppNav
import tv.dustypig.dustypig.ui.theme.DustyPigTheme
import java.lang.ref.WeakReference

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DustyPigTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    AppStateSwitcher()
                }
            }
        }
    }


    @Composable
    fun AppStateSwitcher() {

        AuthManager.init()

        if (AuthManager.loginState == AuthManager.LOGIN_STATE_LOGGED_IN) {
            AppNav()
        } else if (AuthManager.loginState == AuthManager.LOGIN_STATE_LOGGED_OUT) {
            AuthNav()
        }
    }
}
