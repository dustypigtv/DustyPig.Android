package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.PlayerView
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase

@Composable
fun PlayerScreen(vm: PlayerViewModel) {
    val uiState by vm.uiState.collectAsState()
    PlayerScreenInternal(
        popBackStack = vm::popBackStack,
        hideError = vm::hideError,
        uiState = uiState
    )

}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun PlayerScreenInternal(
    popBackStack: () -> Unit,
    hideError: () -> Unit,
    uiState: PlayerUIState
) {
    var lifecycle by remember {
        mutableStateOf(Lifecycle.Event.ON_CREATE)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold { paddingValues ->

        AndroidView(
            factory = { context ->
                PlayerView(context).also {
                    it.player = uiState.player
                }
            },
            update = {
                when (lifecycle) {
                    Lifecycle.Event.ON_PAUSE -> {
                        it.onPause()
                        it.player?.pause()
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        it.onResume()
                    }

                    else -> Unit
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding((paddingValues))
        )
    }

    if(uiState.showErrorDialog) {
        ErrorDialog(
            onDismissRequest = hideError,
            message = uiState.errorMessage
        )
    }
}


@Preview
@Composable
private fun PlayerScreenPreview() {
    val uiState = PlayerUIState (

    )
    PreviewBase {
        PlayerScreenInternal(
            popBackStack = { },
            hideError = { },
            uiState = uiState
        )
    }
}