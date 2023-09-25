package tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.ui.composables.BasicMediaView
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddToPlaylistScreen(vm: AddToPlaylistViewModel) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState: AddToPlaylistUIState by vm.uiState.collectAsState()
    val listState = rememberLazyListState()
    var newName by remember { mutableStateOf("")}
    val enableSaveButton by remember {
        derivedStateOf {
            !uiState.loading && newName.isNotBlank()
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = vm::popBackStack, text = stringResource(R.string.add_to_playlist))
        }
    ) { innerPadding ->

        if(uiState.loading) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
            }


        } else {

            Box (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp, 0.dp),
                contentAlignment = Alignment.TopCenter
            ) {

                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    state = listState
                ) {
                    item {

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                placeholder = { Text(text = stringResource(R.string.new_playlist_name)) },
                                label = { Text(text = stringResource(R.string.new_playlist_name)) },
                                singleLine = true,
                                enabled = !uiState.busy,
                                modifier = Modifier.width(300.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                            )

                            Button(
                                onClick = { vm.newPlaylist(newName) },
                                enabled = enableSaveButton,
                                modifier = Modifier.width(300.dp)
                            ) {
                                Text(text = stringResource(R.string.save))
                            }


                            if (uiState.playlists.isNotEmpty()) {
                                Row (
                                  modifier = Modifier.height(IntrinsicSize.Min)
                                ) {
                                    Box (
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Divider()
                                        Text(
                                            text = buildString {
                                                append("   ")
                                                append(stringResource(R.string.or_choose_a_playlist_below))
                                                append("   ")
                                            },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.background)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }


                    items(uiState.playlists) {
                        val id = it.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp), shape = RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    if (!uiState.busy)
                                        vm.selectPlaylist(id)
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
                                    routeNavigator = vm,
                                    navigateOnClick = false,
                                    enabled = false,
                                    clicked = null
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

                if(uiState.busy)
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            }
        }
    }



    if(uiState.showErrorDialog) {
        ErrorDialog(
            onDismissRequest = vm::hideError,
            message = uiState.errorMessage
        )
    }

}
















