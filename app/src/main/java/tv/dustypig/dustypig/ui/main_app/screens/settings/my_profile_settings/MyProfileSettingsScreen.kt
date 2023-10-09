package tv.dustypig.dustypig.ui.main_app.screens.settings.my_profile_settings

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.job
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.composables.Avatar
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.PinEntry
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon

@Composable
fun MyProfileSettingsScreen(vm: MyProfileSettingsViewModel) {
    val uiState by vm.uiState.collectAsState()

    MyProfileSettingsScreenInternal(
        popBackStack = vm::popBackStack,
        hideError = vm::hideError,
        renameProfile = vm::renameProfile,
        setPinNumber = vm::setPinNumber,
        uiState = uiState
    )

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MyProfileSettingsScreenInternal(
    popBackStack: () -> Unit,
    hideError: () -> Unit,
    renameProfile: (String) -> Unit,
    setPinNumber: (String) -> Unit,
    uiState: MyProfileSettingsUIState
) {

    var showRenameDialog by remember {
        mutableStateOf(false)
    }

    var showChangePinDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = popBackStack, text = "My Profile")
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp, 0.dp)
                        .clip(shape = RoundedCornerShape(4.dp))
                        .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = RoundedCornerShape(4.dp)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){

                    Text(
                        text = uiState.name ,
                        modifier = Modifier
                            .padding(start = 12.dp, top = 12.dp, end = 0.dp, bottom = 12.dp)
                            .weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        enabled = !uiState.busy,
                        onClick = { showRenameDialog = true  },
                        modifier = Modifier.padding(12.dp)
                    ) {
                        TintedIcon(imageVector = Icons.Filled.Edit)
                    }
                }

                Button(
                    onClick = { showChangePinDialog = true },
                    enabled = !uiState.busy,
                    modifier = Modifier
                        .width(200.dp)
                        .padding(24.dp)
                ) {
                    Text(text = "Change PIN #")
                }

                Avatar(
                    imageUrl = uiState.avatarUrl,
                    modifier = Modifier
                        .size(296.dp)
                        .padding(48.dp)
                )

                Button(
                    onClick = { },
                    enabled = !uiState.busy,
                    modifier = Modifier.width(200.dp)
                ) {
                    Text(text = "Upload Image")
                }
            }

            if(uiState.busy) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if(showRenameDialog) {

                val keyboardController = LocalSoftwareKeyboardController.current
                val focusRequester = remember { FocusRequester() }


                var profileName by remember{ mutableStateOf(uiState.name) }

                val submitEnabled by remember {
                    derivedStateOf {
                        profileName.isNotBlank()
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
                    showRenameDialog = false
                    renameProfile(profileName)
                }

                fun dismissClicked() {
                    keyboardController?.hide()
                    showRenameDialog = false
                }

                LaunchedEffect(true) {
                    this.coroutineContext.job.invokeOnCompletion {
                        focusRequester.requestFocus()
                    }
                }

                AlertDialog(
                    shape = RoundedCornerShape(8.dp),
                    onDismissRequest = ::dismissClicked,
                    title = { Text(text = "Rename Profile") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)) {
                            OutlinedTextField(
                                value = profileName,
                                onValueChange = { profileName = it },
                                label = { Text(text = "Name") },
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

            if(showChangePinDialog) {

                var newPin by remember {
                    mutableStateOf("")
                }
                val submitEnabled by remember {
                    derivedStateOf {
                        newPin.isEmpty() || newPin.length == 4
                    }
                }


                fun submitClicked() {
                    showChangePinDialog = false
                    setPinNumber(newPin)
                }

                fun dismissClicked() {
                    showChangePinDialog = false
                }

                AlertDialog(
                    shape = RoundedCornerShape(8.dp),
                    onDismissRequest = ::dismissClicked,
                    title = { Text(text = "Change PIN #") },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            PinEntry(
                                allowEmpty = true,
                                valueChanged = { newPin = it },
                                onSubmit = { submitClicked() }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            enabled = submitEnabled,
                            onClick = { submitClicked() }
                        ) {
                            Text(stringResource(R.string.ok))
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



            if (uiState.showError) {
                ErrorDialog(
                    onDismissRequest = hideError,
                    message = uiState.errorMessage
                )
            }
        }
    }
}


@Preview
@Composable
private fun MyProfileSettingsScreenPreview() {
    val uiState = MyProfileSettingsUIState(
        busy = false,
        name = "The Pig"
    )

    PreviewBase {
        MyProfileSettingsScreenInternal(
            popBackStack = { },
            hideError = { },
            renameProfile = { _ -> },
            setPinNumber = { _ -> },
            uiState = uiState
        )
    }
}