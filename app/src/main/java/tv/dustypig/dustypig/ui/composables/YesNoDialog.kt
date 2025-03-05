package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import tv.dustypig.dustypig.R


@Composable
fun YesNoDialog(
    onNo: () -> Unit,
    onYes: () -> Unit,
    title: String,
    message: String, yes:
    String = stringResource(R.string.yes),
    no: String = stringResource(R.string.no),
    dismissOnClickOutside: Boolean = true
) {
    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        onDismissRequest = onNo,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onYes) {
                Text(yes)
            }
        },
        dismissButton = {
            TextButton(onClick = onNo) {
                Text(no)
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnClickOutside,
            dismissOnClickOutside = dismissOnClickOutside
        )
    )
}


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun YesNoDialogPreview() {
    PreviewBase {
        YesNoDialog(
            onNo = { },
            onYes = { },
            title = "Confirm Stuff",
            message = "Are you sure you want to confirm stuff?"
        )
    }
}
