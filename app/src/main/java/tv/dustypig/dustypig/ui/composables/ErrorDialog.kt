package tv.dustypig.dustypig.ui.composables

import androidx.compose.runtime.Composable

@Composable
fun ErrorDialog(onDismissRequest: () -> Unit, message:String) = OkDialog(onDismissRequest = onDismissRequest, title = "Error", message = message)