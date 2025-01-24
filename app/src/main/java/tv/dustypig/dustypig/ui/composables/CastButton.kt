package tv.dustypig.dustypig.ui.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import tv.dustypig.dustypig.global_managers.cast_manager.CastConnectionState
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager


@Composable
@OptIn(UnstableApi::class)
fun CastButton(castManager: CastManager?) {

    if (castManager == null)
        return

    val castState by castManager.castState.collectAsState()
    if (!castState.castPossible())
        return

    var showPicker by remember {
        mutableStateOf(false)
    }

    if (castState.castConnectionState == CastConnectionState.Busy) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(40.dp)
                .padding(8.dp),
            color = Color.White
        )
    } else {
        IconButton(
            onClick = {
                castManager.setActiveScanning()
                showPicker = true
            }
        ) {
            Icon(
                imageVector =
                if (castState.castConnectionState == CastConnectionState.Connected)
                    Icons.Filled.CastConnected
                else
                    Icons.Filled.Cast,
                contentDescription = null,
                tint = Color.White
            )
        }

        if (showPicker) {
            CastDialog(
                closeDialog = { showPicker = false },
                castManager = castManager
            )
        }
    }
}