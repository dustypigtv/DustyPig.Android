package tv.dustypig.dustypig.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import tv.dustypig.dustypig.R

@Composable
fun ErrorDialog(onDismissRequest: () -> Unit, message:String?) = OkDialog(
    onDismissRequest = onDismissRequest,
    title = stringResource(R.string.error),
    message = message ?: stringResource(R.string.unknown_error)
)