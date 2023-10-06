package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R

@Composable
fun OkDialog (onDismissRequest: () -> Unit, title:String, message:String) {
    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}


@Preview
@Composable
private fun OkDialogPreview() {
    PreviewBase {
        OkDialog(onDismissRequest = { }, title = "Title", message = "Message")
    }
}