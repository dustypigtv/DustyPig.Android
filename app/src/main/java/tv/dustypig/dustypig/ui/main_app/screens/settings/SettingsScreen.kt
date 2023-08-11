package tv.dustypig.dustypig.ui.main_app.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.AuthManager
import tv.dustypig.dustypig.nav.NavRoute


@Composable
fun SettingsScreen(vm: SettingsViewModel) {

    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ){
        Text(text = "You have logged in")
        Button(onClick = { AuthManager.setAuthState(context, "", 0, false ) }) {
            Text(text = "Log Out")
        }
    }
}