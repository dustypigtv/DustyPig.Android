package tv.dustypig.dustypig.ui.auth_flow.screens.sign_up

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OkDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon

@Composable
fun SignUpScreen(vm: SignUpViewModel) {
    val uiState by vm.uiState.collectAsState()
    SignUpScreenInternal(uiState = uiState)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SignUpScreenInternal(uiState: SignUpUIState) {

    val localFocusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(uiState.email) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val signUpEnabled by remember {
        derivedStateOf {
            !uiState.busy && name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
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


    val visualTransformation by remember {
        derivedStateOf {
            if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation()
        }
    }

    val imeAction = remember {
        derivedStateOf {
            if (name.isBlank() || email.isBlank() || password.isBlank())
                ImeAction.Done
            else
                ImeAction.Go
        }
    }

    fun signUp() {
        localFocusManager.clearFocus()
        keyboardController?.hide()
        uiState.onSignUp(name, email, password)
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
                contentDescription = ""
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(text = stringResource(R.string.name)) },
                label = { Text(text = stringResource(R.string.name)) },
                singleLine = true,
                enabled = !uiState.busy,
                modifier = Modifier.width(300.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim().lowercase() },
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
                    onGo = { signUp() },
                    onDone = { keyboardController?.hide() }),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        TintedIcon(imageVector = iconImage)
                    }
                },
                visualTransformation = visualTransformation
            )

            Button(
                enabled = signUpEnabled,
                modifier = Modifier.width(120.dp),
                onClick = ::signUp
            ) {
                Text(text = stringResource(R.string.sign_up))
            }

            TextButton(
                enabled = !uiState.busy,
                onClick = { uiState.onNavToSignIn(email) }
            ) {
                Text(text = stringResource(R.string.already_have_an_account_sign_in))
            }

        }

        if (uiState.busy) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }


    if (uiState.showError) {
        ErrorDialog(onDismissRequest = uiState.onHideError, message = uiState.errorMessage)
    }

    if (uiState.showSuccess) {
        OkDialog(
            onDismissRequest = { uiState.onNavToSignIn(email) },
            title = stringResource(R.string.success),
            message = uiState.message
        )
    }
}


@Preview
@Composable
private fun SignUpScreenPreview() {
    val uiState = SignUpUIState(busy = false)
    PreviewBase {
        SignUpScreenInternal(uiState = uiState)
    }
}

















