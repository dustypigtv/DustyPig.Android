package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PinEntry(allowEmpty: Boolean = false, valueChanged: (String) -> Unit, onSubmit: (String) -> Unit) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val pinList = remember {
        listOf(
            mutableStateOf(""),
            mutableStateOf(""),
            mutableStateOf(""),
            mutableStateOf("")
        )
    }

    val pinListAllowBackspace = remember {
        listOf(
            mutableStateOf(false),
            mutableStateOf(false),
            mutableStateOf(false),
            mutableStateOf(false)
        )
    }

    val pinFocusRequesters = listOf(
        FocusRequester(),
        FocusRequester(),
        FocusRequester(),
        FocusRequester()
    )

    var currentPin by remember { mutableStateOf("") }

    val imeAction by remember {
        derivedStateOf {
            if(currentPin.length == 4)
                ImeAction.Go
            else if(allowEmpty && currentPin.isEmpty())
                ImeAction.Go
            else
                ImeAction.Done
        }
    }

    fun constructPin() {

        var pin = ""
        for(i in pinList.indices) {
            if(pinList[i].value.isEmpty()) {
                break
            }
            pin += pinList[i].value
        }
        currentPin = pin
        valueChanged(pin)
    }

    fun pinValueChanged(index: Int, newValue: String) {

        if (newValue.length > 1) {
            return
        }

        pinListAllowBackspace[index].value = pinList[index].value.isEmpty()
        pinList[index].value = newValue
        constructPin()

        if(newValue.isEmpty())
            return

        if(index < 3) {
            for (i in (index + 1)..3) {
                if (pinList[i].value == "") {
                    pinFocusRequesters[i].requestFocus()
                    pinListAllowBackspace[i].value = pinList[i].value.isEmpty()
                    break
                }
            }
        }

    }

    fun keyPressed(keyEvent: KeyEvent, index: Int): Boolean{

        if(index > 0 && keyEvent.key == Key.Backspace && pinListAllowBackspace[index].value) {
            pinList[index - 1].value = ""
            constructPin()
            pinListAllowBackspace[index - 1].value = true
            pinFocusRequesters[index - 1].requestFocus()
            return true
        }
        else if(pinList[index].value.isEmpty()) {
            pinListAllowBackspace[index].value = true
        }

        return false
    }

    LaunchedEffect(true) {
        pinFocusRequesters[0].requestFocus()
    }

    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()){
        repeat(4){index ->
            OutlinedTextField(
                modifier = Modifier
                    .width(40.dp)
                    .focusRequester(pinFocusRequesters[index])
                    .onKeyEvent { keyEvent -> keyPressed(keyEvent, index) },
                singleLine = true,
                textStyle = TextStyle(textAlign = TextAlign.Center),
                value = pinList[index].value,
                onValueChange = { newValue -> pinValueChanged(index, newValue) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = imeAction),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() },
                    onGo = { onSubmit(currentPin) }
                ),
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }

}





@Preview
@Composable
private fun PinEntryPreview() {
    PreviewBase {
        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = { },
            title = { Text(stringResource(R.string.enter_pin)) },
            text = {
                PinEntry(
                    valueChanged = {  },
                    onSubmit = { }
                )
            },
            confirmButton = {
                TextButton(onClick = {  }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

























