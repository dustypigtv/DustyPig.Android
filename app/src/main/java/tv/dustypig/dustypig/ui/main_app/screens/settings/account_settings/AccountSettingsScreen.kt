package tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(vm: AccountSettingsViewModel) {

    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = vm::popBackStack, text = "Account Settings")
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier.padding(paddingValues)
        ) {

        }
    }
}
