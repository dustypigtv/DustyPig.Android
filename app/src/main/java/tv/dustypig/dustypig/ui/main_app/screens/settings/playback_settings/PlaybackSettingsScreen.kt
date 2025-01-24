package tv.dustypig.dustypig.ui.main_app.screens.settings.playback_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.PreviewBase

@Composable
fun PlaybackSettingsScreen(vm: PlaybackSettingsViewModel) {
    val uiState by vm.uiState.collectAsState()
    PlaybackSettingsScreenInternal(uiState = uiState)
}

@Composable
private fun PlaybackSettingsScreenInternal(
    uiState: PlaybackSettingsUIState
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                onClick = uiState.onPopBackStack,
                text = stringResource(R.string.playback_settings)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp, 0.dp)
                    .clip(shape = RoundedCornerShape(4.dp))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = stringResource(R.string.auto_skip_intros),
                    modifier = Modifier.padding(12.dp)
                )

                Switch(
                    checked = uiState.autoSkipIntros,
                    onCheckedChange = uiState.onSetAutoSkipIntros,
                    modifier = Modifier.padding(12.dp, 8.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp, 0.dp)
                    .clip(shape = RoundedCornerShape(4.dp))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = stringResource(R.string.auto_skip_credits),
                    modifier = Modifier.padding(12.dp)
                )

                Switch(
                    checked = uiState.autoSkipCredits,
                    onCheckedChange = uiState.onSetAutoSkipCredits,
                    modifier = Modifier.padding(12.dp, 8.dp)
                )
            }


            Text(
                text = stringResource(R.string.this_only_works_for_videos_that_have_intro_and_credit_timings),
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}


@Preview
@Composable
private fun PlaybackSettingsScreenPreview() {
    val uiState = PlaybackSettingsUIState(
        autoSkipCredits = true
    )
    PreviewBase {
        PlaybackSettingsScreenInternal(uiState = uiState)
    }
}