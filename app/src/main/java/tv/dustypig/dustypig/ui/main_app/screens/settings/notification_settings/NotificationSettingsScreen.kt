package tv.dustypig.dustypig.ui.main_app.screens.settings.notification_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.OkDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase

@Composable
fun NotificationSettingsScreen(vm: NotificationSettingsViewModel) {
    val uiState by vm.uiState.collectAsState()
    NotificationSettingsScreenInternal(uiState = uiState)
}

@Composable
private fun NotificationSettingsScreenInternal(uiState: NotificationSettingsUIState) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = uiState.onPopBackStack, text = stringResource(R.string.notification_settings))
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp, 0.dp)
                    .clip(shape = RoundedCornerShape(4.dp))
                    .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = RoundedCornerShape(4.dp))
            ) {
                Text(
                    text = stringResource(R.string.allow_notifications),
                    modifier = Modifier.padding(12.dp)
                )

                Switch(
                    checked = uiState.allowAlerts,
                    onCheckedChange = uiState.onSetAllowAlerts,
                    modifier = Modifier.padding(12.dp, 8.dp)
                )
            }

            Text(
                text = stringResource(R.string.allow_alerts_description),
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    if(uiState.showAlertsDialog) {
        OkDialog(
            onDismissRequest = { uiState.onHideAlertsDialog(context) },
            title = stringResource(R.string.action_required),
            message = stringResource(R.string.allow_system_notifications_instructions)
        )
    }
}

@Preview
@Composable
private fun NotificationSettingsScreenPreview() {
    val uiState = NotificationSettingsUIState ()
    PreviewBase {
        NotificationSettingsScreenInternal(uiState = uiState)
    }
}