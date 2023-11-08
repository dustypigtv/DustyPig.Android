package tv.dustypig.dustypig.ui.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.cast_manager.CastConnectionState
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastPlaybackStatus


@Composable
@OptIn(UnstableApi::class)
fun CastButton(castManager: CastManager?) {

    if (castManager == null)
        return

    val castButtonState by castManager.castButtonState.collectAsState()
    if (castButtonState == CastConnectionState.Unavailable)
        return

    val castState by castManager.castState.collectAsState()

    var showPicker by remember {
        mutableStateOf(false)
    }

    if (castButtonState == CastConnectionState.Busy) {
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
                    if(castButtonState == CastConnectionState.Connected)
                        Icons.Filled.CastConnected
                    else
                        Icons.Filled.Cast,
                contentDescription = null,
                tint = Color.White
            )
        }

        if (showPicker) {
            AlertDialog(
                shape = RoundedCornerShape(8.dp),
                onDismissRequest = {
                    castManager.setPassiveScanning()
                    showPicker = false
                },
                title = {
                    if(castButtonState == CastConnectionState.Connected) {
                        Text(text = "Casting to ${castState.selectedRoute?.name}")
                    } else {
                        Text(text = "Cast to")
                    }
                },
                text = {
                    LazyColumn(
                        state = rememberLazyListState()
                    ) {

                        if (castButtonState == CastConnectionState.Connected) {
                            item {
                                Row (
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {

                                    //Prevent flicker
                                    var artworkUrl by remember {
                                        mutableStateOf(castState.artworkUrl)
                                    }
                                    if(artworkUrl != castState.artworkUrl)
                                        artworkUrl = castState.artworkUrl

                                    if(!(artworkUrl.isNullOrBlank())) {
                                        AsyncImage(
                                            model = castState.artworkUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.FillHeight,
                                            alignment = Alignment.CenterStart,
                                            modifier = Modifier
                                                .background(
                                                    Color.DarkGray,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .clip(RoundedCornerShape(4.dp)),
                                            error = painterResource(id = R.drawable.error_tall)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if(castState.playbackStatus == CastPlaybackStatus.Stopped) {
                                            Text(text = "Nothing Playing")
                                        } else {
                                            Text(
                                                text = castState.title ?: "Unknown Title",
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            LinearProgressIndicator(
                                                progress = castState.progress,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 4.dp)
                                            )
                                        }
                                    }

                                }
                            }

                        } else if(castState.availableRoutes.isEmpty()) {
                            item {
                                Row (
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Text(text = "Looking for Cast devices")
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .padding(8.dp)
                                    )
                                }
                            }
                        } else {
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
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if(castButtonState == CastConnectionState.Connected) {
                        TextButton(
                            onClick = {
                                castManager.setPassiveScanning()
                                showPicker = false
                                castManager.disconnect()
                            }
                        ) {
                            Text(text = "Stop Casting")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            castManager.setPassiveScanning()
                            showPicker = false
                        }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            )
        }
    }
}