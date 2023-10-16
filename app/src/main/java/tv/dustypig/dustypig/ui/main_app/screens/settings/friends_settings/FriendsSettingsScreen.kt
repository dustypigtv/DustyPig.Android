package tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import tv.dustypig.dustypig.api.models.BasicFriend
import tv.dustypig.dustypig.ui.composables.Avatar
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OkDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon

@Composable
fun FriendsSettingsScreen(vm: FriendsSettingsViewModel) {
    val uiState by vm.uiState.collectAsState()
    FriendsSettingsScreenInternal(
        popBackStack = vm::popBackStack,
        navToFriendDetails = vm::navToFriendDetails,
        hideDialogs = vm::hideDialog,
        addFriend = vm::addFriend,
        uiState = uiState
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FriendsSettingsScreenInternal(
    popBackStack: () -> Unit,
    navToFriendDetails: (Int) -> Unit,
    hideDialogs: () -> Unit,
    addFriend: (String) -> Unit,
    uiState: FriendsSettingsUIState
) {

    var showAddFriendDialog by remember {
        mutableStateOf(false)
    }

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = popBackStack, text = stringResource(R.string.manage_friends))
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                item{
                    //Just some blank space between top and actual list
                }

                items(uiState.friends, key = { it.clientUUID }) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp, 0.dp)
                            .clip(shape = RoundedCornerShape(4.dp))
                            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = RoundedCornerShape(4.dp))
                            .clickable { navToFriendDetails(it.id) }
                    ) {
                        Avatar(
                            imageUrl = it.avatarUrl,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(48.dp),
                            clickable = false
                        )

                        Text(
                            text = it.displayName,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        TintedIcon(
                            imageVector = Icons.Filled.ChevronRight,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                item {
                    Button(
                        onClick = { showAddFriendDialog = true },
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(text = "Add Friend")
                    }
                }

            }

            if(uiState.busy) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }



        if (showAddFriendDialog) {

            val keyboardController = LocalSoftwareKeyboardController.current
            var email by remember{ mutableStateOf("") }
            val focusRequester = remember { FocusRequester() }

            val submitEnabled by remember {
                derivedStateOf {
                    email.isNotBlank() && !uiState.inviteBusy
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
                showAddFriendDialog = false
                addFriend(email)
            }

            fun dismissClicked() {
                keyboardController?.hide()
                showAddFriendDialog = false
            }

            LaunchedEffect(true) {
                this.coroutineContext.job.invokeOnCompletion {
                    focusRequester.requestFocus()
                }
            }

            AlertDialog(
                shape = RoundedCornerShape(8.dp),
                onDismissRequest = ::dismissClicked,
                title = { Text(text = stringResource(R.string.invite_friend)) },
                text = {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it.trim().lowercase() },
                                label = { Text(text = stringResource(R.string.email)) },
                                singleLine = true,
                                enabled = !uiState.inviteBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = imeAction),
                                keyboardActions = KeyboardActions(onGo = { submitClicked() }, onDone = { keyboardController?.hide() })
                            )
                        }

                        if (uiState.inviteBusy) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

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
                        enabled = !uiState.inviteBusy,
                        onClick = ::dismissClicked
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }


        if(uiState.showInviteSuccessDialog) {
            OkDialog(onDismissRequest = hideDialogs, title = "Add Friend", message = "Invite Sent")
        }


        if(uiState.showError) {
            ErrorDialog(onDismissRequest = hideDialogs, message = uiState.errorMessage)
        }
    }
}


@Preview
@Composable
private fun FriendSettingsScreenPreview() {
    val uiState = FriendsSettingsUIState(
        friends = listOf(
            BasicFriend(
                id = 0,
                displayName = "John Doe 1",
                avatarUrl = ""
            ),
            BasicFriend(
                id = 1,
                displayName = "John Doe 2",
                avatarUrl = ""
            ),
            BasicFriend(
                id = 2,
                displayName = "John Doe 3",
                avatarUrl = ""
            )
        )
    )

    PreviewBase {
        FriendsSettingsScreenInternal(
            popBackStack = { },
            navToFriendDetails = { _ -> },
            hideDialogs = { },
            addFriend = { _ -> },
            uiState = uiState
        )
    }
}




















