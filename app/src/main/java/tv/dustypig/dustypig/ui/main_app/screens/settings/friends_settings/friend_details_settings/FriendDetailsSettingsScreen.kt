package tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.friend_details_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.job
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.composables.Avatar
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.TintedIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun FriendDetailsSettingsScreen(vm: FriendDetailsSettingsViewModel) {

    val uiState by vm.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showChangeDisplayName by remember {
        mutableStateOf(false)
    }

    Scaffold (
        topBar = {
            CommonTopAppBar(onClick = vm::popBackStack, text = "Friend Info")
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                item {
                    //Spacer between top and content
                }

                /**
                 * Display Name
                 */
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

                        Avatar(
                            imageUrl = uiState.avatarUrl,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(48.dp),
                            clickable = false
                        )

                        Text(
                            text = uiState.displayName,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            enabled = !showChangeDisplayName,
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


                /**
                 * Libs shared with me
                 */

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
            vm.changeDisplayName(newName = displayName)
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
            title = { Text(stringResource(R.string.forgot_password)) },
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
        ErrorDialog(onDismissRequest = vm::hideError, message = uiState.errorMessage)
    }
}