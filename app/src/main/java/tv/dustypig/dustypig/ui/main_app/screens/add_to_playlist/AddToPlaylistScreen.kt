package tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.ui.composables.BasicMediaView
import tv.dustypig.dustypig.ui.composables.ErrorDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddToPlaylistScreen(vm: AddToPlaylistViewModel) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState: AddToPlaylistUIState by vm.uiState.collectAsState()
    val listState = rememberLazyListState()
    val newName = remember { mutableStateOf("")}

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add To Playlist",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { vm.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    ) { innerPadding ->

        if(uiState.busy) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
            }


        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = listState
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = newName.value,
                            onValueChange = { newName.value = it },
                            placeholder = { Text(text = "New Playlist Name") },
                            label = { Text(text = "New Playlist Name") },
                            singleLine = true,
                            enabled = !uiState.busy,
                            modifier = Modifier.width(300.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                        )

                        Button(
                            onClick = { vm.newPlaylist(newName.value) },
                            enabled = !uiState.busy,
                            modifier = Modifier.width(300.dp)
                        ) {
                            Text(text = "Save")
                        }

                        if (uiState.playlists.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Or Choose a Playlist Below")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                items(uiState.playlists) {
                    val id = it.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
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
                                    title = it.name
                                ),
                                routeNavigator = vm,
                                clicked = { vm.selectPlaylist(id) },
                                modifier = Modifier.background(Color.Red)
                            )
                        }

                        Text(
                            text = it.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.selectPlaylist(id) },
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }



    if(uiState.showError) {
        ErrorDialog(
            onDismissRequest = {
                vm.hideError(uiState.criticalError)
            },
            message = uiState.errorMessage
        )
    }

}





















