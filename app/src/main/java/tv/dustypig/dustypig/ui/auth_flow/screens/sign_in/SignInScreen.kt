package tv.dustypig.dustypig.ui.auth_flow.screens.sign_in

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.job
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.auth_manager.AuthManager
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OkDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon

@Composable
fun SignInScreen(vm: SignInViewModel) {
    val uiState by vm.uiState.collectAsState()
    SignInScreenInternal(uiState = uiState)
}


@Composable
private fun SignInScreenInternal(uiState: SignInUIState) {
    val localFocusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var email by remember { mutableStateOf(uiState.emailAddress) }
    var password by remember { mutableStateOf("") }
    var showForgotPassword by remember { mutableStateOf(false) }

    val visualTransform by remember {
        derivedStateOf {
            if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation()
        }
    }

    val iconImage by remember {
        derivedStateOf {
            if (passwordVisible)
                Icons.Filled.VisibilityOff
            else
                Icons.Filled.Visibility
        }
    }

    val imeAction = remember {
        derivedStateOf {
            if (email.isBlank() || password.isBlank())
                ImeAction.Done
            else
                ImeAction.Go
        }
    }

    val signInEnabled = !uiState.busy && email.isNotBlank() && password.isNotBlank()


    fun updateEmail(newValue: String) {
        email = newValue.trim().lowercase()
        if (email == AuthManager.TEST_USER)
            password = AuthManager.TEST_PASSWORD
    }


    fun signIn() {
        localFocusManager.clearFocus()
        keyboardController?.hide()
        uiState.onSignIn(email, password)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                12.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo_transparent),
                modifier = Modifier.size(100.dp),
                contentDescription = null
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    if(!showForgotPassword) {
                        updateEmail(it)
                    }
                },
                placeholder = { Text(text = stringResource(R.string.email)) },
                label = { Text(text = stringResource(R.string.email)) },
                singleLine = true,
                enabled = !uiState.busy,
                modifier = Modifier.width(300.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(text = stringResource(R.string.password)) },
                label = { Text(text = stringResource(R.string.password)) },
                singleLine = true,
                enabled = !uiState.busy,
                modifier = Modifier.width(300.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = imeAction.value
                ),
                keyboardActions = KeyboardActions(
                    onGo = { signIn() },
                    onDone = { keyboardController?.hide() }),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        TintedIcon(imageVector = iconImage)
                    }
                },
                visualTransformation = visualTransform
            )

            Row(
                modifier = Modifier.width(300.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    enabled = !uiState.busy,
                    onClick = { showForgotPassword = true }
                ) {
                    Text(text = stringResource(R.string.forgot_password_question))
                }

                Button(
                    enabled = signInEnabled,
                    onClick = ::signIn,
                    modifier = Modifier.width(120.dp),
                ) {
                    Text(text = stringResource(R.string.sign_in))
                }
            }

            TextButton(
                enabled = !uiState.busy,
                onClick = { uiState.onNavToSignUp(email) }
            ) {
                Text(text = stringResource(R.string.don_t_have_an_account_sign_up))
            }
        }

        if (uiState.busy) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }


    if (showForgotPassword) {

        val focusRequester = remember { FocusRequester() }

        val confirmEnabled by remember {
            derivedStateOf {
                email.isNotBlank() && !uiState.forgotPasswordBusy
            }
        }

        val forgotPasswordImeAction by remember {
            derivedStateOf {
                if (confirmEnabled)
                    ImeAction.Go
                else
                    ImeAction.Done
            }
        }


        fun forgotPasswordConfirmClick() {
            keyboardController?.hide()
            uiState.onSendForgotPasswordEmail(email)
        }

        fun dismissForgotPasswordDialog() {
            keyboardController?.hide()
            showForgotPassword = false
        }

        LaunchedEffect(true) {
            this.coroutineContext.job.invokeOnCompletion {
                focusRequester.requestFocus()
            }
        }

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = ::dismissForgotPasswordDialog,
            title = { Text(stringResource(R.string.forgot_password)) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            20.dp,
                            alignment = Alignment.CenterVertically
                        )
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                if(showForgotPassword) {
                                    email = it.trim().lowercase()
                                }
                            },
                            label = { Text(text = stringResource(R.string.email)) },
                            singleLine = true,
                            enabled = !uiState.forgotPasswordBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = forgotPasswordImeAction
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = { forgotPasswordConfirmClick() },
                                onDone = { keyboardController?.hide() })
                        )
                    }

                    if (uiState.forgotPasswordBusy) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                }
            },
            confirmButton = {
                TextButton(
                    enabled = confirmEnabled,
                    onClick = ::forgotPasswordConfirmClick
                ) {
                    Text(stringResource(R.string.submit))
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.forgotPasswordBusy,
                    onClick = ::dismissForgotPasswordDialog
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }



    if (uiState.showError) {
        ErrorDialog(onDismissRequest = uiState.onHideError, message = uiState.errorMessage)
    }

    if (uiState.showForgotPasswordSuccess) {
        OkDialog(
            onDismissRequest = uiState.onHideForgotPasswordSuccess,
            title = stringResource(R.string.email_sent),
            message = stringResource(R.string.check_your_email_for_password_reset_instructions)
        )
    }

    if (uiState.showForgotPasswordError) {
        ErrorDialog(
            onDismissRequest = uiState.onHideForgotPasswordError,
            message = uiState.errorMessage
        )
    }

}

@Preview
@Composable
private fun SignInScreenPreview() {
    val uiState = SignInUIState(busy = false)
    PreviewBase {
        SignInScreenInternal(uiState = uiState)
    }
}