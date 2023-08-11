package tv.dustypig.dustypig.ui.auth_flow.screens.sign_up

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.composables.OkDialog


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignUpScreen(vm: SignUpViewModel) {

    val uiState by vm.uiState.collectAsState()
    val localFocusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordVisible = remember { mutableStateOf(false) }
    val signUpEnabled = remember { derivedStateOf { !uiState.busy && uiState.name.isNotBlank() && uiState.email.isNotBlank() && uiState.password.isNotBlank() }}

    val imeAction = remember { derivedStateOf {
        if(uiState.name.isBlank() || uiState.email.isBlank() || uiState.password.isBlank()) {
            ImeAction.Done
        }
        else {
            ImeAction.Go
        }
    }}

    fun signUp() {
        localFocusManager.clearFocus()
        keyboardController?.hide()
        vm.signUp()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {


        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo),
            modifier = Modifier.size(100.dp),
            contentDescription = ""
        )

        OutlinedTextField(
            value = uiState.name,
            onValueChange = { vm.updateName(it) },
            placeholder = { Text(text = "Name") },
            label = { Text(text = "Name") },
            singleLine = true,
            enabled = !uiState.busy,
            modifier = Modifier.width(300.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { vm.updateEmail(it) },
            placeholder = { Text(text = "Email") },
            label = { Text(text = "Email") },
            singleLine = true,
            enabled = !uiState.busy,
            modifier = Modifier.width(300.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { vm.updatePassword(it) },
            placeholder = { Text(text = "Password") },
            label = { Text(text = "Password") },
            singleLine = true,
            enabled = !uiState.busy,
            modifier = Modifier.width(300.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction.value),
            keyboardActions = KeyboardActions(onGo = { signUp() }, onDone = { keyboardController?.hide() }),
            trailingIcon = {
                val iconImage = if (passwordVisible.value) {
                    Icons.Filled.VisibilityOff
                } else {
                    Icons.Filled.Visibility
                }

                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(imageVector = iconImage, "")
                }
            },
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation()
        )

        Button(enabled = signUpEnabled.value,
            modifier = Modifier.size(120.dp, 40.dp),
            onClick = { signUp() }) {
            if (uiState.busy) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(text = "Sign Up")
            }
        }

        TextButton(onClick = { vm.navToSignIn() }) {
            Text(text = "Already have an account? Sign In")
        }

    }


    if (uiState.showError) {
        OkDialog(onDismissRequest = { vm.hideError() }, title = "Error", message = uiState.message)
    }

    if (uiState.showSuccess) {
        OkDialog(onDismissRequest = { vm.navToSignIn() }, title = "Success", message = uiState.message)
    }
}

