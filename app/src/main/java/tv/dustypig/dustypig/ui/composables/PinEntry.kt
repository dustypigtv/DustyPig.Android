package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PinEntry(valueChanged: (String) -> Unit, autoFocus: Boolean = false) {

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

    fun constructPin() {

        var pin = ""
        for(i in pinList.indices) {
            if(pinList[i].value.isEmpty()) {
                return
            }
            pin += pinList[i].value
        }

        valueChanged.invoke(pin)
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

    if(autoFocus){
        LaunchedEffect(true) {
            delay(300)
            pinFocusRequesters[0].requestFocus()
        }
    }

    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()){
        repeat(4){index ->
            OutlinedTextField(
                modifier = Modifier
                    .width(40.dp)
                    .focusRequester(pinFocusRequesters[index])
                    .onKeyEvent {keyEvent -> keyPressed(keyEvent, index) },
                singleLine = true,
                textStyle = TextStyle(textAlign = TextAlign.Center),
                value = pinList[index].value,
                onValueChange = { newValue -> pinValueChanged(index, newValue) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }

}