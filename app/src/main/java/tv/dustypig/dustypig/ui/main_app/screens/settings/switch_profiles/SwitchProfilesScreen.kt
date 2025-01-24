package tv.dustypig.dustypig.ui.main_app.screens.settings.switch_profiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.ui.composables.Avatar
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.PinEntry
import tv.dustypig.dustypig.ui.composables.PreviewBase

@Composable
fun SwitchProfilesScreen(vm: SwitchProfilesViewModel) {
    val uiState by vm.uiState.collectAsState()
    SwitchProfilesScreenInternal(uiState = uiState)
}

@Composable
private fun SwitchProfilesScreenInternal(
    uiState: SwitchProfilesUIState
) {
    val listState = rememberLazyGridState()
    var pin by remember { mutableStateOf("") }
    var showPinDialog by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf<BasicProfile?>(null) }


    Scaffold(
        topBar = {
            CommonTopAppBar(
                onClick = uiState.onPopBackStack,
                text = stringResource(R.string.select_profile)
            )
        }
    ) { contentPadding ->

        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 72.dp),
                verticalArrangement = Arrangement.spacedBy(36.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 24.dp),
                state = listState
            ) {
                items(uiState.profiles, key = { it.id }) {

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Avatar(
                            basicProfile = it,
                            clickable = !uiState.busy,
                            onClick = {
                                if (it.hasPin) {
                                    selectedProfile = it
                                    showPinDialog = true
                                } else {
                                    uiState.onSignIn(it, null)
                                }
                            },
                            size = 48
                        )
                        Text(text = it.name)
                    }
                }
            }

            if (uiState.busy) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (showPinDialog) {

        val confirmEnabled by remember {
            derivedStateOf {
                pin.length == 4 && !uiState.busy
            }
        }

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = { showPinDialog = false },
            title = { Text(stringResource(R.string.enter_pin)) },
            text = {
                PinEntry(
                    valueChanged = { pin = it },
                    onSubmit = {
                        pin = it
                        uiState.onSignIn(selectedProfile!!, pin.toUShortOrNull())
                    }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = confirmEnabled,
                    onClick = { uiState.onSignIn(selectedProfile!!, pin.toUShortOrNull()) }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPinDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (uiState.showError) {
        ErrorDialog(onDismissRequest = uiState.onHideError, message = uiState.errorMessage)
    }
}


@Preview
@Composable
private fun SwitchProfilesScreenPreview() {
    val uiState = SwitchProfilesUIState(
        busy = false,
        profiles = listOf(
            BasicProfile(
                id = 1,
                name = "Test 1",
                initials = "T1",
                avatarUrl = "https://s3.dustypig.tv/user-art-defaults/profile/blue.png",
                isMain = true,
                hasPin = true
            ),
            BasicProfile(
                id = 2,
                name = "Test 2",
                initials = "T2",
                avatarUrl = "https://s3.dustypig.tv/user-art-defaults/profile/gold.png",
                isMain = false,
                hasPin = false
            )
        )
    )
    PreviewBase {
        SwitchProfilesScreenInternal(uiState = uiState)
    }
}