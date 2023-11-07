package tv.dustypig.dustypig.ui.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager


@Composable
@OptIn(UnstableApi::class)
fun CastButton(castManager: CastManager?) {

    if(castManager == null)
        return

    val castState by castManager.state.collectAsState()
    if(castState.castAvailable) {

        var showPicker by remember {
            mutableStateOf(false)
        }

        val icon = if (castState.connected) Icons.Filled.CastConnected else Icons.Filled.Cast


        IconButton(
            onClick = {
                castManager.setActiveScanning()
                showPicker = true
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White
            )
        }

        if(showPicker) {
            val lazyListState = rememberLazyListState()
            AlertDialog(
                shape = RoundedCornerShape(8.dp),
                onDismissRequest = {
                    castManager.setPassiveScanning()
                    showPicker = false
                },
                title = {
                    Text(text = "Cast to")
                },
                text = {
                    LazyColumn(
                        state = lazyListState
                    ) {

                        if(castState.connected) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            castManager.disconnect()
                                            castManager.setPassiveScanning()
                                            showPicker = false
                                        }
                                ) {
                                    TintedIcon(
                                        imageVector = Icons.Filled.PhoneAndroid,
                                        modifier = Modifier.padding(12.dp)
                                    )

                                    Text(
                                        text = "Stop Casting",
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Divider()
                                }
                            }
                        }

                        items(castState.availableRoutes) { routeInfo ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        routeInfo.select()
                                        castManager.setPassiveScanning()
                                        showPicker = false
                                    }
                            ) {
                                TintedIcon(
                                    imageVector = Icons.Filled.Tv,
                                    modifier = Modifier.padding(12.dp)
                                )

                                Text(
                                    text = routeInfo.name,
                                    modifier = Modifier.padding(12.dp)
                                )

                                if(routeInfo.id == castState.selectedRoute.id) {
                                    TintedIcon(
                                        imageVector = Icons.Filled.Check
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            castManager.setPassiveScanning()
                            showPicker = false
                        }
                    ) {
                        Text(text = "Cancel")
                    }
                },
            )
        }
    }
}