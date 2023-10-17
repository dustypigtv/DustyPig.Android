package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import tv.dustypig.dustypig.ui.composables.PreviewBase

@Composable
fun PlayerScreen(vm: PlayerViewModel) {
    val uiState by vm.uiState.collectAsState()
    PlayerScreenInternal(
        popBackStack = vm::popBackStack,
        uiState = uiState
    )

}

@Composable
private fun PlayerScreenInternal(
    popBackStack: () -> Unit,
    uiState: PlayerUIState
) {

    val context = LocalContext.current


}


@Preview
@Composable
private fun PlayerScreenPreview() {
    val uiState = PlayerUIState (

    )
    PreviewBase {
        PlayerScreenInternal(
            popBackStack = { },
            uiState = uiState
        )
    }
}