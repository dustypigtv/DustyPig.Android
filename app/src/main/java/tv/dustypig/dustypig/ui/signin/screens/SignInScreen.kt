package tv.dustypig.dustypig.ui.signin.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.AuthManager
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.ThePig
import tv.dustypig.dustypig.api.models.LoginResponse
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.models.SimpleValue
import tv.dustypig.dustypig.api.throwIfError
import tv.dustypig.dustypig.ui.composables.OkDialog
import tv.dustypig.dustypig.ui.signin.SignInDestinations


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignInScreen(navHostController: NavHostController, email: MutableState<String>, password:MutableState<String>) {

    val context = LocalContext.current
    val localFocusManager = LocalFocusManager.current
    val composableScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordVisible = remember { mutableStateOf(false) }
    val busy = remember { mutableStateOf(false) }
    val showLoginSpinner = remember { mutableStateOf(false) }
    val showForgotPasswordDialog = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }
    val showError = remember { mutableStateOf(false) }
    val showForgotError = remember { mutableStateOf(false) }
    val showForgotSuccess = remember { mutableStateOf(false) }
    val signInEnabled = remember { derivedStateOf { !busy.value && email.value.isNotBlank() && password.value.isNotBlank() }}
    val imeAction = remember { derivedStateOf { if(email.value.isBlank() || password.value.isBlank() ) ImeAction.Done else ImeAction.Go }}


    fun resetBusy() {
        busy.value = false
        showLoginSpinner.value = false
    }


    fun forgotPasswordSuccess(newEmail: String) {
        email.value = newEmail
        showForgotPasswordDialog.value = false
        showForgotSuccess.value = true
    }

    fun forgotPasswordError(error: String) {
        showForgotPasswordDialog.value = false
        errorMessage.value = error
        showForgotError.value = true
    }

    fun passwordSignIn() {

        localFocusManager.clearFocus()
        keyboardController?.hide()

        if (!signInEnabled.value)
            return

        busy.value = true
        showLoginSpinner.value = true

        composableScope.launch {
            try {

                val response = ThePig.api.passwordLogin(
                    PasswordCredentials(
                        email.value,
                        password.value,
                        null
                    )
                )
                response.throwIfError()

                val data = response.body()!!.data
                if (data.login_type == LoginResponse.LOGIN_TYPE_ACCOUNT) {
                    AuthManager.setTempAuthToken(data.token!!)
                    navHostController.navigate(SignInDestinations.SelectProfile)
                } else {
                    AuthManager.setAuthState(context, data.token!!, data.profile_id!!, data.login_type == LoginResponse.LOGIN_TYPE_MAIN_PROFILE)
                }

            } catch (ex: Exception) {
                resetBusy()
                errorMessage.value = ex.message ?: "Unknown Error"
                showError.value = true
            }
        }
    }


    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {


        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo),
            modifier = Modifier.size(100.dp),
            contentDescription = null
        )

        OutlinedTextField(
            value = email.value,
            onValueChange = {
                email.value = it.lowercase().trim()
                if(email.value == AuthManager.TEST_USER) {
                    password.value = AuthManager.TEST_PASSWORD
                }
            },
            placeholder = { Text(text = "Email") },
            label = { Text(text = "Email") },
            singleLine = true,
            enabled = !busy.value,
            modifier = Modifier.width(300.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            placeholder = { Text(text = "Password") },
            label = { Text(text = "Password") },
            singleLine = true,
            enabled = !busy.value,
            modifier = Modifier.width(300.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction.value),
            keyboardActions = KeyboardActions(onGo = { passwordSignIn() }, onDone = { keyboardController?.hide() }),
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

        Row(
            modifier = Modifier.width(300.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(enabled = !busy.value,
                onClick = {
                    keyboardController?.hide()
                    showForgotPasswordDialog.value = showForgotPasswordDialog.value.not()
                }) {
                Text(text = "Forgot Password?")
            }

            Button(enabled = signInEnabled.value,
                modifier = Modifier.size(120.dp, 40.dp),
                onClick = { passwordSignIn() }) {
                if (showLoginSpinner.value) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(text = "Sign In")
                }
            }
        }

        TextButton(enabled = !busy.value,
            onClick = { navHostController.navigate(SignInDestinations.SignUp) }) {
            Text(text = "Don't have an account? Sign Up")
        }
    }

    if (showForgotPasswordDialog.value) {
        ForgotPasswordDialog(onDismissRequest = { showForgotPasswordDialog.value = false },
            onSuccess = { forgotPasswordSuccess(it) },
            onError = { forgotPasswordError(it) })
    }

    if (showError.value) {
        OkDialog(onDismissRequest = { showError.value = false }, title = "Error", message = errorMessage.value)
    }

    if (showForgotSuccess.value) {
        OkDialog(onDismissRequest = { showForgotSuccess.value = false }, title = "Email Sent", message = "Check your email for password reset instructions")
    }

    if (showForgotError.value) {
        OkDialog(
            onDismissRequest = {
                showForgotError.value = false
                showForgotPasswordDialog.value = true
            },
            title = "Error",
            message = errorMessage.value
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun ForgotPasswordDialog(onDismissRequest: () -> Unit, onSuccess: (String) -> Unit, onError: (String) -> Unit) {

    val localFocusManager = LocalFocusManager.current
    val composableScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val forgottenEmail = remember { mutableStateOf("") }
    val forgotPasswordBusy = remember { mutableStateOf(false) }
    val focusRequester = FocusRequester()
    val confirmEnabled = remember { derivedStateOf { forgottenEmail.value.isNotBlank() && !forgotPasswordBusy.value }}
    val imeAction = remember { derivedStateOf { if(confirmEnabled.value) ImeAction.Go else ImeAction.Done }}

    fun forgotPasswordConfirmClick() {

        localFocusManager.clearFocus()
        keyboardController?.hide()

        if (forgotPasswordBusy.value || forgottenEmail.value == "")
            return

        forgotPasswordBusy.value = true
        composableScope.launch {
            try {
                val response = ThePig.api.sendPasswordResetEmail(SimpleValue(forgottenEmail.value))
                response.throwIfError()
                onSuccess.invoke(forgottenEmail.value)
            } catch (ex: Exception) {
                onError.invoke(ex.localizedMessage ?: "Unknown Error")
            }
        }
    }

    LaunchedEffect(true) {
        delay(300)
        focusRequester.requestFocus()
    }

    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text("Forgot Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)) {
                Text("Enter your email address")
                OutlinedTextField(
                    value = forgottenEmail.value,
                    onValueChange = { forgottenEmail.value = it },
                    placeholder = { Text(text = "Email") },
                    label = { Text(text = "Email") },
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
                if (forgotPasswordBusy.value) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Submit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Preview
@Composable
fun SignInScreenPreview() {
    SignInScreen(rememberNavController(), remember{mutableStateOf("")}, remember{mutableStateOf("")})
}
