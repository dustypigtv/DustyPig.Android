package tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import tv.dustypig.dustypig.api.models.OverrideState
import tv.dustypig.dustypig.api.models.ProfileTitleOverrideInfo
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase

@Composable
fun ManageParentalControlsForTitleScreen(vm: ManageParentalControlsForTitleViewModel) {
    val uiState by vm.uiState.collectAsState()
    ManageParentalControlsForTitleScreenInternal(uiState = uiState)
}


@Composable
private fun ManageParentalControlsForTitleScreenInternal(uiState: ManageParentalControlsForTitleUIState) {

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = uiState.onPopBackStack, text = stringResource(R.string.parental_controls))
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = listState
            ) {

                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 0.dp)
                            .clip(shape = RoundedCornerShape(4.dp))
                            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = RoundedCornerShape(4.dp)),

                        ) {
                        Text(
                            text = stringResource(R.string.profile),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.allowed),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                /**
                 * Sub Profiles
                 */
                if (uiState.subProfiles.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.padding(24.dp, 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Divider(
                                modifier = Modifier.height(1.dp)
                            )
                            Text(
                                modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                                text = "   Profiles   ",
                            )
                        }
                    }


                    items(uiState.subProfiles) { info ->

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp, 0.dp)
                        ) {
                            Text(
                                text = info.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(12.dp, 0.dp)
                            )

                            Switch(
                                modifier = Modifier.padding(12.dp, 0.dp),
                                checked = info.overrideState == OverrideState.Allow,
                                enabled = !uiState.busy,
                                onCheckedChange = { uiState.onTogglePermission(info.profileId) })
                        }

                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                }


                /**
                 * Friends
                 */
                if (uiState.friendProfiles.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.padding(24.dp, 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Divider(
                                modifier = Modifier.height(1.dp)
                            )
                            Text(
                                modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                                text = "   Friends   ",
                            )
                        }
                    }

                    items(uiState.friendProfiles) { info ->

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp, 0.dp)
                        ) {
                            Text(
                                text = info.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(12.dp, 0.dp)
                            )

                            Switch(
                                modifier = Modifier.padding(12.dp, 0.dp),
                                checked = info.overrideState == OverrideState.Allow,
                                enabled = !uiState.busy,
                                onCheckedChange = { uiState.onTogglePermission(info.profileId) })
                        }

                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

            }

            if(uiState.busy) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

        }
    }


    if(uiState.showErrorDialog) {
        ErrorDialog(
            onDismissRequest = uiState.onHideError,
            message = uiState.errorMessage
        )
    }

}


@Preview
@Composable
private fun ManageParentalControlsForTitleScreenPreview() {

    val uiState = ManageParentalControlsForTitleUIState(
        subProfiles = listOf(
            ProfileTitleOverrideInfo(
                profileId = 0,
                overrideState = OverrideState.Allow,
                name = "Profile 1"
            ),
            ProfileTitleOverrideInfo(
                profileId = 1,
                overrideState = OverrideState.Block,
                name = "Profile 2"
            )
        ),
        friendProfiles = listOf(
            ProfileTitleOverrideInfo(
                profileId = 2,
                overrideState = OverrideState.Allow,
                name = "Friend 1"
            ),
            ProfileTitleOverrideInfo(
                profileId = 3,
                overrideState = OverrideState.Block,
                name = "Friend 2"
            )
        )
    )

    PreviewBase {
        ManageParentalControlsForTitleScreenInternal(uiState = uiState)
    }
}