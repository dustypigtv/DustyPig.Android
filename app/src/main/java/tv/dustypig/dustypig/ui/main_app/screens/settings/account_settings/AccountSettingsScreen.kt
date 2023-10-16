package tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.job
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OkDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.composables.YesNoDialog
import tv.dustypig.dustypig.ui.theme.BurntOrange

@Composable
fun AccountSettingsScreen(vm: AccountSettingsViewModel) {

    val uiState by vm.uiState.collectAsState()
    AccountSettingsScreenInternal(
        popBackStack = vm::popBackStack,
        hideError = vm::hideError,
        signOut = vm::signOut,
        loginToDevice = vm::loginToDevice,
        changePassword = vm::changePassword,
        hideChangePasswordSuccess = vm::hideChangePasswordDialogs,
        signOutEverywhere = vm::signOutEverywhere,
        deleteAccount = vm::deleteAccount,
        uiState = uiState
    )

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AccountSettingsScreenInternal(
    popBackStack: () -> Unit,
    hideError: () -> Unit,
    signOut: () -> Unit,
    loginToDevice: (String) -> Unit,
    changePassword: (String) -> Unit,
    hideChangePasswordSuccess: () -> Unit,
    signOutEverywhere: () -> Unit,
    deleteAccount: () -> Unit,
    uiState: AccountSettingsUIState
) {

    val buttonModifier = Modifier.width(320.dp)
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var showLoginToDeviceDialog by remember {
        mutableStateOf(false)
    }

    var showChangePasswordDialog by remember {
        mutableStateOf(false)
    }

    var showSignoutEverywhereDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteAccountDialog by remember {
        mutableStateOf(false)
    }



    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = popBackStack, text = stringResource(R.string.account_settings))
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                Spacer(Modifier.width(24.dp))

                Button(
                    onClick = { showLoginToDeviceDialog = true },
                    modifier = buttonModifier,
                    enabled = !uiState.busy
                ) {
                    Text(text = stringResource(R.string.login_to_another_device))
                }


                if(uiState.isMainProfile) {
                    Button(
                        onClick = { showChangePasswordDialog = true },
                        modifier = buttonModifier,
                        enabled = !uiState.busy
                    ) {
                        Text(text = stringResource(R.string.change_password))
                    }
                }

                Button(
                    onClick = signOut,
                    modifier = buttonModifier,
                    enabled = !uiState.busy
                ) {
                    Text(text = stringResource(R.string.logout))
                }

                if(uiState.isMainProfile) {
                    Button(
                        onClick = { showSignoutEverywhereDialog = true },
                        modifier = buttonModifier,
                        enabled = !uiState.busy
                    ) {
                        Text(text = stringResource(R.string.logout_of_all_devices))
                    }

                    Button(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = buttonModifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ),
                        enabled = !uiState.busy
                    ) {
                        Text(text = stringResource(R.string.delete_account))
                    }
                }
            }

            if(uiState.busy) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if(showLoginToDeviceDialog) {

        var code by remember {
            mutableStateOf("")
        }

        val enableOk by remember {
            derivedStateOf{
                code.length == 5 && !uiState.busy
            }
        }

        val imeAction by remember {
            derivedStateOf {
                if(enableOk)
                    ImeAction.Go
                else
                    ImeAction.Done
            }
        }

        fun loginToDevice() {
            keyboardController?.hide()
            if (enableOk) {
                loginToDevice(code)
            }
        }

        fun dismissDialog() {
            keyboardController?.hide()
            showLoginToDeviceDialog = false
        }


        LaunchedEffect(true) {
            this.coroutineContext.job.invokeOnCompletion {
                focusRequester.requestFocus()
            }
        }

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = ::dismissDialog,
            title = { Text(text = stringResource(R.string.login_to_another_device)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text(text = stringResource(R.string.device_code)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = imeAction, capitalization = KeyboardCapitalization.Characters),
                        keyboardActions = KeyboardActions(
                            onGo = { loginToDevice() },
                            onDone = { keyboardController?.hide() }
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = enableOk,
                    onClick = ::loginToDevice
                ) {
                    Text(text = stringResource(R.string.submit))
                }
            },
            dismissButton = {
                TextButton(onClick = ::dismissDialog) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )

    }

    if(showChangePasswordDialog) {
        var newPassword by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        val iconImage by remember {
            derivedStateOf {
                if (passwordVisible) {
                    Icons.Filled.VisibilityOff
                } else {
                    Icons.Filled.Visibility
                }
            }
        }

        val visualTransform by remember {
            derivedStateOf {
                if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation()
            }
        }

        val enableOk by remember {
            derivedStateOf{
                newPassword.isNotBlank() && !uiState.busy
            }
        }

        val imeAction by remember {
            derivedStateOf {
                if(enableOk)
                    ImeAction.Go
                else
                    ImeAction.Done
            }
        }

        fun dismissDialog() {
            passwordVisible = false
            keyboardController?.hide()
            showChangePasswordDialog = false
        }

        fun changePassword() {
            passwordVisible = false
            keyboardController?.hide()
            if(enableOk) {
                changePassword(newPassword)
            }
        }

        LaunchedEffect(true) {
            this.coroutineContext.job.invokeOnCompletion {
                focusRequester.requestFocus()
            }
        }

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = ::dismissDialog,
            title = { Text(text = stringResource(R.string.change_password)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)) {

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(text = stringResource(R.string.new_password)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
                        keyboardActions = KeyboardActions(onGo = { changePassword() }, onDone = { keyboardController?.hide() }),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                TintedIcon(imageVector = iconImage)
                            }
                        },
                        visualTransformation = visualTransform
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = enableOk,
                    onClick = ::changePassword
                ) {
                    Text(text = stringResource(R.string.submit))
                }
            },
            dismissButton = {
                TextButton(onClick = ::dismissDialog) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )


    }

    if(uiState.showChangePasswordSuccessAlert) {
        OkDialog(
            onDismissRequest = hideChangePasswordSuccess,
            title = stringResource(R.string.success),
            message = stringResource(R.string.your_password_was_successfully_changed)
        )
    }

    if(showSignoutEverywhereDialog) {
        YesNoDialog(
            onNo = { showSignoutEverywhereDialog = false },
            onYes = {
                showSignoutEverywhereDialog = false
                signOutEverywhere()
            },
            title = stringResource(R.string.logout_of_all_devices),
            message = stringResource(R.string.are_you_sure_you_want_to_force_all_devices_to_log_out)
        )
    }

    if(showDeleteAccountDialog) {

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {Text(text = stringResource(R.string.delete_account)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)
                ) {
                    Text(
                        text = stringResource(R.string.warning),
                        color = BurntOrange,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(text = "This will immediately delete all profiles and data, and is irreversible. Are you sure you want to delete your account?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = stringResource(R.string.delete_account_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false  }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }


    if(uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = hideError, message = uiState.errorMessage)
    }

}

@Preview
@Composable
private fun AccountSettingsScreenPreview() {

    val uiState = AccountSettingsUIState(
        busy = false,
        isMainProfile = true
    )

    PreviewBase {
        AccountSettingsScreenInternal(
            popBackStack = { },
            hideError = { },
            signOut = { },
            loginToDevice = { _ -> },
            changePassword = { _ -> },
            hideChangePasswordSuccess = { },
            signOutEverywhere = { },
            deleteAccount = { },
            uiState = uiState
        )
    }
}