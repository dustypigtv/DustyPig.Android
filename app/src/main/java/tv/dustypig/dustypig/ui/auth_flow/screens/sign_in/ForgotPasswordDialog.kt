package tv.dustypig.dustypig.ui.auth_flow.screens.sign_in

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import tv.dustypig.dustypig.R


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
internal fun ForgotPasswordDialog(vm: SignInViewModel) {

    val uiState by vm.uiState.collectAsState()
    val localFocusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()
    val confirmEnabled = remember { derivedStateOf { uiState.email.isNotBlank() && !uiState.busy } }
    val imeAction = remember { derivedStateOf { if(confirmEnabled.value) ImeAction.Go else ImeAction.Done } }

    fun forgotPasswordConfirmClick() {
        localFocusManager.clearFocus()
        keyboardController?.hide()
        vm.sendForgotPasswordEmail()
    }

    LaunchedEffect(true) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        onDismissRequest = { vm.hideForgotPassword() },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(stringResource(R.string.forgot_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)) {
                Text(stringResource(R.string.enter_your_email_address))
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { vm.updateEmail(it) },
                    placeholder = { Text(text = stringResource(R.string.email)) },
                    label = { Text(text = stringResource(R.string.email)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = imeAction.value),
                    keyboardActions = KeyboardActions(onGo = { forgotPasswordConfirmClick() }, onDone = { keyboardController?.hide() })
                )
            }
        },
        confirmButton = {
            TextButton(enabled = confirmEnabled.value,
                onClick = { forgotPasswordConfirmClick() }) {
                if (uiState.forgotPasswordBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(stringResource(R.string.submit))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.hideForgotPassword() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
