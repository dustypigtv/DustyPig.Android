package tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.friend_details_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.job
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicLibrary
import tv.dustypig.dustypig.ui.composables.Avatar
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon

@Composable
fun FriendDetailsSettingsScreen(vm: FriendDetailsSettingsViewModel) {
    val uiState by vm.uiState.collectAsState()
    FriendDetailsSettingsScreenInternal(uiState = uiState)
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FriendDetailsSettingsScreenInternal(uiState: FriendDetailsSettingsUIState) {

    val listState = rememberLazyListState()
    var showChangeDisplayName by remember {
        mutableStateOf(false)
    }

    Scaffold (
        topBar = {
            CommonTopAppBar(onClick = uiState.onPopBackStack, text = "Friend Info")
        }
    ) { paddingValues ->

        Box (
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                /**
                 * Display Name
                 */
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp, 0.dp)
                            .clip(shape = RoundedCornerShape(4.dp))
                            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = RoundedCornerShape(4.dp)),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){

                        Box(
                            modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp)
                        ) {
                            Avatar(
                                imageUrl = uiState.avatarUrl,
                                size = 48,
                                clickable = false
                            )
                        }

                        Text(
                            text = uiState.displayName,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            enabled = !showChangeDisplayName && !uiState.busy,
                            onClick = { showChangeDisplayName = true },
                            modifier = Modifier.padding(12.dp)
                        ) {
                            TintedIcon(imageVector = Icons.Filled.Edit)
                        }
                    }
                }


                /**
                 * Libs shared with friend
                 */
                if(uiState.myLibs.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        Box(
                            modifier = Modifier.padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            HorizontalDivider()
                            Text(
                                modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                                text = "   Shared Libraries   ",
                            )
                        }
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clip(shape = RoundedCornerShape(4.dp))
                                .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = RoundedCornerShape(4.dp)),

                            ) {
                            Text(
                                modifier = Modifier.padding(12.dp),
                                text = "Library",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                modifier = Modifier.padding(12.dp),
                                text = "Shared",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    items(uiState.myLibs) { lib ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp, 0.dp)
                        ) {
                            Text(
                                modifier = Modifier.padding(12.dp, 0.dp),
                                text = lib.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Switch(
                                modifier = Modifier.padding(12.dp, 0.dp),
                                checked = lib.shared,
                                enabled = !uiState.busy,
                                onCheckedChange = { uiState.onToggleLibraryShare(lib.id) }
                            )
                        }
                    }
                }




                /**
                 * Libs shared with me
                 */
                if(uiState.libsSharedWithMe.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        Box(
                            modifier = Modifier.padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            HorizontalDivider()
                            Text(
                                modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                                text = "   Libraries Shared With Me   ",
                            )
                        }
                    }
                    items(uiState.libsSharedWithMe) { lib ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.padding(12.dp, 0.dp),
                                text = lib.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }


                item {
                    Button(
                        onClick = uiState.onUnfriend,
                        enabled = !uiState.busy,
                        modifier = Modifier.padding(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(text = "Unfriend")
                    }
                }
            }

            if(uiState.busy) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if(showChangeDisplayName) {
        val keyboardController = LocalSoftwareKeyboardController.current
        var displayName by remember{ mutableStateOf(uiState.displayName) }
        val focusRequester = remember { FocusRequester() }

        val submitEnabled by remember {
            derivedStateOf {
                displayName.isNotBlank()
            }
        }

        val imeAction by remember {
            derivedStateOf {
                if (submitEnabled)
                    ImeAction.Go
                else
                    ImeAction.Done
            }
        }


        fun submitClicked() {
            keyboardController?.hide()
            showChangeDisplayName = false
            uiState.onChangeDisplayName(displayName)
        }

        fun dismissClicked() {
            keyboardController?.hide()
            showChangeDisplayName = false
        }

        LaunchedEffect(true) {
            this.coroutineContext.job.invokeOnCompletion {
                focusRequester.requestFocus()
            }
        }

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = ::dismissClicked,
            title = { Text(text = "Change Display Name") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text(text = stringResource(R.string.display_name)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = imeAction),
                        keyboardActions = KeyboardActions(onGo = { submitClicked() }, onDone = { keyboardController?.hide() })
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = submitEnabled,
                    onClick = ::submitClicked
                ) {
                    Text(stringResource(R.string.submit))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = ::dismissClicked
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if(uiState.showError) {
        ErrorDialog(onDismissRequest = uiState.onHideError, message = uiState.errorMessage)
    }
}


@Preview
@Composable
private fun FriendDetailsSettingsScreenPreview() {
    val uiState = FriendDetailsSettingsUIState(
        busy = false,
        displayName = "Display Name",
        libsSharedWithMe = listOf(
            BasicLibrary(
                id = 0,
                name = "Shared Movies Lib",
                isTV = false
            ),
            BasicLibrary(
                id = 1,
                name = "Shared TV Lib",
                isTV = true
            )
        ),
        myLibs = listOf(
            ShareableLibrary(
                id = 2,
                name = "My Movies Lib",
                isTV = false,
                shared = false,
            ),
            ShareableLibrary(
                id = 3,
                name = "My TV Lib",
                isTV = true,
                shared = true
            )
        )
    )

    PreviewBase {
        FriendDetailsSettingsScreenInternal(uiState = uiState)
    }
}






























