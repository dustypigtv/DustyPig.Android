package tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.job
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicPlaylist
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.ui.composables.BasicMediaView
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.YesNoDialog

@Composable
fun AddToPlaylistScreen(vm: AddToPlaylistViewModel) {
    val uiState: AddToPlaylistUIState by vm.uiState.collectAsState()
    AddToPlaylistScreenInternal(uiState = uiState)
}

@Composable
private fun AddToPlaylistScreenInternal(uiState: AddToPlaylistUIState) {

    val listState = rememberLazyListState()
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var showAutoEpisodesDialog by remember { mutableStateOf(false) }
    var newPlaylistMode by remember { mutableStateOf(false) }
    var selectedId by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                onClick = uiState.onPopBackStack,
                text = stringResource(R.string.add_to_playlist)
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {

            LazyColumn(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = listState
            ) {

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                if (!uiState.busy) {
                                    newPlaylistMode = true
                                    showNewPlaylistDialog = true
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .height(150.dp)
                                .width(100.dp)
                                .background(
                                    Color.DarkGray,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clip(RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        )
                        {

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = null,
                                modifier = Modifier.size(75.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = stringResource(R.string.new_playlist),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                }

                items(uiState.playlists) {
                    val id = it.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                if (!uiState.busy) {
                                    if (uiState.addingSeries) {
                                        newPlaylistMode = false
                                        selectedId = id
                                        showAutoEpisodesDialog = true
                                    } else {
                                        uiState.onSelectPlaylist(id, false)
                                    }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .height(150.dp)
                                .width(100.dp),
                            contentAlignment = Alignment.TopStart
                        )
                        {
                            BasicMediaView(
                                basicMedia = BasicMedia(
                                    id = it.id,
                                    mediaType = MediaTypes.Playlist,
                                    artworkUrl = it.artworkUrl,
                                    backdropUrl = "",
                                    title = it.name
                                ),
                                enabled = false,
                            )
                        }

                        Text(
                            text = it.name,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (uiState.busy) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }


    val keyboardController = LocalSoftwareKeyboardController.current
    var newName by remember { mutableStateOf("") }
    fun confirmNewPlaylistClicked() {
        keyboardController?.hide()
        showNewPlaylistDialog = false
        if (uiState.addingSeries) {
            showAutoEpisodesDialog = true
        } else {
            uiState.onNewPlaylist(newName, false)
        }
    }


    if(showNewPlaylistDialog) {
        val focusRequester = remember { FocusRequester() }

        val confirmEnabled by remember {
            derivedStateOf {
                newName.isNotBlank() && !uiState.busy
            }
        }

        fun dismissShowNewPlaylistDialog() {
            keyboardController?.hide()
            showNewPlaylistDialog = false
        }

        LaunchedEffect(true) {
            this.coroutineContext.job.invokeOnCompletion {
                focusRequester.requestFocus()
            }
        }

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = ::dismissShowNewPlaylistDialog,
            title = { Text(stringResource(R.string.new_playlist)) },
            text = {
                Box(
                    modifier = Modifier.width(300.dp)
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(text = stringResource(R.string.name)) },
                        singleLine = true,
                        enabled = !uiState.busy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )

                    if (uiState.busy) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = confirmEnabled,
                    onClick = ::confirmNewPlaylistClicked
                ) {
                    Text(stringResource(R.string.submit))
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.busy,
                    onClick = ::dismissShowNewPlaylistDialog
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )

    }

    if (showAutoEpisodesDialog) {
        YesNoDialog(
            onNo = {
                showAutoEpisodesDialog = false
                if (newPlaylistMode) {
                    uiState.onNewPlaylist(newName, false)
                } else {
                    uiState.onSelectPlaylist(selectedId, false)
                }
            },
            onYes = {
                showAutoEpisodesDialog = false
                if (newPlaylistMode) {
                    uiState.onNewPlaylist(newName, true)
                } else {
                    uiState.onSelectPlaylist(selectedId, true)
                }
            },
            title = stringResource(R.string.new_episodes),
            message = stringResource(R.string.new_episodes_message)
        )
    }


    if (uiState.showErrorDialog) {
        ErrorDialog(
            onDismissRequest = uiState.onHideError,
            message = uiState.errorMessage
        )
    }

}


@Preview(showSystemUi = true)
@Composable
private fun AddToPlaylistScreenPreview() {

    val uiState = AddToPlaylistUIState(
        playlists = listOf(
            BasicPlaylist(
                id = 1,
                name = "Playlist 1",
                artworkUrl = "https://s3.dustypig.tv/user-art-defaults/playlist/default.png"
            ),
            BasicPlaylist(
                id = 2,
                name = "Playlist 2",
                artworkUrl = "https://s3.dustypig.tv/user-art-defaults/playlist/default.png"
            ),
            BasicPlaylist(
                id = 3,
                name = "Playlist 3",
                artworkUrl = "https://s3.dustypig.tv/user-art-defaults/playlist/default.png"
            ),
            BasicPlaylist(
                id = 4,
                name = "Playlist 4",
                artworkUrl = "https://s3.dustypig.tv/user-art-defaults/playlist/default.png"
            )
        )
    )

    PreviewBase {
        AddToPlaylistScreenInternal(uiState = uiState)
    }
}







































