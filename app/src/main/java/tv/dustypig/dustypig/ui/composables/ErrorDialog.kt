package tv.dustypig.dustypig.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.settings_manager.Themes
import tv.dustypig.dustypig.ui.theme.DustyPigTheme

@Composable
fun ErrorDialog(onDismissRequest: () -> Unit, message:String?) = OkDialog(
    onDismissRequest = onDismissRequest,
    title = stringResource(R.string.error),
    message = message ?: stringResource(R.string.unknown_error)
)

@Preview
@Composable
private fun ErrorDialogPreview() {
    DustyPigTheme(currentTheme = Themes.Maggies) {
        ErrorDialog(onDismissRequest = { }, message = "This is the error message")
    }
}