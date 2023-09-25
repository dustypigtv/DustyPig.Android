package tv.dustypig.dustypig.ui.auth_flow.screens.sign_in

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OkDialog


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignInScreen(
    vm: SignInViewModel
) {
    val uiState by vm.uiState.collectAsState()
    val localFocusManager = LocalFocusManager.current
    val passwordVisible = remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val imeAction = remember { derivedStateOf { if(uiState.email.isBlank() || uiState.password.isBlank() ) ImeAction.Done else ImeAction.Go }}
    val signInEnabled = remember { derivedStateOf { !uiState.busy && uiState.email.isNotBlank() && uiState.password.isNotBlank() }}

    fun showForgotPassword() {
        localFocusManager.clearFocus()
        keyboardController?.hide()
        vm.showForgotPassword()
    }

    fun signIn() {
        localFocusManager.clearFocus()
        keyboardController?.hide()
        vm.signIn()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo_transparent),
            modifier = Modifier.size(100.dp),
            contentDescription = null
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { vm.updateEmail(it) },
            placeholder = { Text(text = stringResource(R.string.email)) },
            label = { Text(text = stringResource(R.string.email)) },
            singleLine = true,
            enabled = !uiState.busy,
            modifier = Modifier.width(300.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { vm.updatePassword(it) },
            placeholder = { Text(text = stringResource(R.string.password)) },
            label = { Text(text = stringResource(R.string.password)) },
            singleLine = true,
            enabled = !uiState.busy,
            modifier = Modifier.width(300.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction.value),
            keyboardActions = KeyboardActions(onGo = { signIn() }, onDone = { keyboardController?.hide() }),
            trailingIcon = {
                val iconImage = if (passwordVisible.value) {
                    Icons.Filled.VisibilityOff
                } else {
                    Icons.Filled.Visibility
                }

                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(imageVector = iconImage, null)
                }
            },
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation()
        )

        Row(
            modifier = Modifier.width(300.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(enabled = !uiState.busy,
                onClick = { showForgotPassword() }) {
                Text(text = stringResource(R.string.forgot_password_question))
            }

            Button(enabled = signInEnabled.value,
                modifier = Modifier.size(120.dp, 40.dp),
                onClick = { signIn() }) {
                if (uiState.busy) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(text = stringResource(R.string.sign_in))
                }
            }
        }

        TextButton(enabled = !uiState.busy,
            onClick = { vm.navToSignUp() }) {
            Text(text = stringResource(R.string.don_t_have_an_account_sign_up))
        }
    }

    if (uiState.showForgotPassword) {
        ForgotPasswordDialog(vm)
    }

    if (uiState.showError) {
        ErrorDialog(onDismissRequest = { vm.hideError() }, message = uiState.errorMessage)
    }

    if (uiState.showForgotPasswordSuccess) {
        OkDialog(
            onDismissRequest = { vm.hideForgotPasswordSuccess() },
            title = stringResource(R.string.email_sent),
            message = stringResource(R.string.check_your_email_for_password_reset_instructions)
        )
    }

    if(uiState.showForgotPasswordError) {
        ErrorDialog(onDismissRequest = { vm.hideForgotPasswordError()}, message = uiState.errorMessage)
    }
}
