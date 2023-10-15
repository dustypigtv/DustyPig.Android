package tv.dustypig.dustypig.ui.main_app.screens.settings.playback_settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.PreviewBase

@Composable
fun PlaybackSettingsScreen(vm: PlaybackSettingsViewModel) {
    val uiState by vm.uiState.collectAsState()
    PlaybackSettingsScreenInternal(
        popBackStack = vm::popBackStack,
        setAutoSkipIntros = vm::setAutoSkipIntros,
        setAutoSkipCredits = vm::setAutoSkipCredits,
        uiState = uiState
    )
}

@Composable
private fun PlaybackSettingsScreenInternal(
    popBackStack: () -> Unit,
    setAutoSkipIntros: (Boolean) -> Unit,
    setAutoSkipCredits: (Boolean) -> Unit,
    uiState: PlaybackSettingsUIState
) {
    Scaffold (
        topBar = {
            CommonTopAppBar(onClick = popBackStack, text = stringResource(R.string.playback_settings))
        }
    ) { paddingValues ->
        Column (
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            ListItem(
                modifier = Modifier
                    .padding(12.dp)
                    .clip(shape = RoundedCornerShape(4.dp)),
                headlineContent = {
                    Text(text = stringResource(R.string.auto_skip_intros))
                },
                trailingContent = {
                    Switch(
                        checked = uiState.autoSkipIntros,
                        onCheckedChange = setAutoSkipIntros
                    )
                }
            )

            ListItem(
                modifier = Modifier.padding(12.dp),
                headlineContent = {
                    Text(text = stringResource(R.string.auto_skip_credits))
                },
                trailingContent = {
                    Switch(
                        checked = uiState.autoSkipCredits,
                        onCheckedChange = setAutoSkipCredits
                    )
                }
            )
        }
    }
}


@Preview
@Composable
private fun PlaybackSettingsScreenPreview() {
    val uiState = PlaybackSettingsUIState (
        autoSkipCredits = true
    )
    PreviewBase {
        PlaybackSettingsScreenInternal(
            popBackStack = { },
            setAutoSkipIntros = { _ -> },
            setAutoSkipCredits = { _ -> },
            uiState = uiState
        )
    }
}