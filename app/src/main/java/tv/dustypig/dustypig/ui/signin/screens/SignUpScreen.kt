package tv.dustypig.dustypig.ui.signin.screens

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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.ThePig
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.throwIfError
import tv.dustypig.dustypig.ui.composables.OkDialog


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignUpScreen(navHostController: NavHostController, email: MutableState<String>) {

    val localFocusManager = LocalFocusManager.current
    val composableScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val name = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val busy = remember { mutableStateOf(false) }
    val showError = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }
    val requireEmailVerification = remember { mutableStateOf(false) }
    val showSuccess = remember { mutableStateOf(false) }
    val signUpEnabled = remember { derivedStateOf { !busy.value && name.value.isNotBlank() && email.value.isNotBlank() && password.value.isNotBlank()  }}

    val imeAction = remember { derivedStateOf {
        if(name.value.isBlank() || email.value.isBlank() || password.value.isBlank()) {
            ImeAction.Done
        }
        else {
            ImeAction.Go
        }
    }}

    fun signUp() {

        localFocusManager.clearFocus()
        keyboardController?.hide()

        if (!signUpEnabled.value)
            return

        busy.value = true

        composableScope.launch {
            try {
                val response = ThePig.api.createAccount(CreateAccount(email.value, password.value, name.value))
                response.throwIfError()
                val data = response.body()!!.data
                requireEmailVerification.value = data.email_verification_required ?: false
                showSuccess.value = true
            } catch (ex: Exception) {
                busy.value = false
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
            contentDescription = ""
        )

        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            placeholder = { Text(text = "Name") },
            label = { Text(text = "Name") },
            singleLine = true,
            enabled = !busy.value,
            modifier = Modifier.width(300.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
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
            if (busy.value) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(text = "Sign Up")
            }
        }

        TextButton(onClick = { navHostController.popBackStack() }) {
            Text(text = "Already have an account? Sign In")
        }

    }


    if (showError.value) {
        OkDialog(onDismissRequest = { showError.value = false }, title = "Error", message = errorMessage.value)
    }

    if (showSuccess.value) {
        OkDialog(
            onDismissRequest = {
                navHostController.popBackStack()
            },
            title = "Success",
            message = if (requireEmailVerification.value) "Please check your email to complete sign up" else "You may now sign in"
        )
    }
}

@Preview
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(rememberNavController(), remember{ mutableStateOf("") })
}
