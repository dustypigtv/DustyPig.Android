package tv.dustypig.dustypig.ui.main_app.screens.playlist_details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.PlaylistItem
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.settings_manager.Themes
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.MultiDownloadDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.composables.YesNoDialog
import tv.dustypig.dustypig.ui.isTablet
import tv.dustypig.dustypig.ui.theme.DustyPigTheme

@Composable
fun PlaylistDetailsScreen(vm: PlaylistDetailsViewModel) {

    val uiState by vm.uiState.collectAsState()
    PlaylistDetailsScreenInternal(
        popBackStack = vm::popBackStack,
        hideError = vm::hideError,
        listUpdated = vm::listUpdated,
        updateListOrderOnServer = vm::updateListOrderOnServer,
        playUpNext = vm::playUpNext,
        deletePlaylist = vm::deletePlaylist,
        deleteItem = vm::deleteItem,
        playItem = vm::playItem,
        navToItem = vm::navToItem,
        renamePlaylist = vm::renamePlaylist,
        updateDownloads = vm::updateDownloads,
        uiState = uiState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistDetailsScreenInternal(
    popBackStack: () -> Unit,
    hideError: () -> Unit,
    listUpdated: () -> Unit,
    updateListOrderOnServer: (Int, Int) -> Unit,
    playUpNext: () -> Unit,
    deletePlaylist: () -> Unit,
    deleteItem: (Int) -> Unit,
    playItem: (Int) -> Unit,
    navToItem: (Int) -> Unit,
    renamePlaylist: (String) -> Unit,
    updateDownloads: (Int) -> Unit,
    uiState: PlaylistDetailsUIState
) {

    val showRenameDialog = remember {
        mutableStateOf(false)
    }

    val showDownloadDialog = remember {
        mutableStateOf(false)
    }

    val showDeletePlaylistDialog = remember {
        mutableStateOf(false)
    }

    val criticalError by remember {
        derivedStateOf {
            uiState.showErrorDialog && uiState.criticalError
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = popBackStack, text = stringResource(R.string.playlist_info))
        }
    ) { innerPadding ->

        OnDevice(
            onPhone = {
                PhoneLayout(
                    listUpdated = listUpdated,
                    updateListOrderOnServer = updateListOrderOnServer,
                    playUpNext = playUpNext,
                    deleteItem = deleteItem,
                    playItem = playItem,
                    navToItem = navToItem,
                    showRenameDialog = showRenameDialog,
                    showDownloadDialog = showDownloadDialog,
                    showDeletePlaylistDialog = showDeletePlaylistDialog,
                    uiState = uiState,
                    criticalError = criticalError,
                    innerPadding = innerPadding
                )
            },
            onTablet = {
                OnOrientation(
                    onPortrait = {
                        PhoneLayout(
                            listUpdated = listUpdated,
                            updateListOrderOnServer = updateListOrderOnServer,
                            playUpNext = playUpNext,
                            deleteItem = deleteItem,
                            playItem = playItem,
                            navToItem = navToItem,
                            showRenameDialog = showRenameDialog,
                            showDownloadDialog = showDownloadDialog,
                            showDeletePlaylistDialog = showDeletePlaylistDialog,
                            uiState = uiState,
                            criticalError = criticalError,
                            innerPadding = innerPadding
                        )
                    },
                    onLandscape = {
                        HorizontalTabletLayout(
                            listUpdated = listUpdated,
                            updateListOrderOnServer = updateListOrderOnServer,
                            playUpNext = playUpNext,
                            deleteItem = deleteItem,
                            playItem = playItem,
                            navToItem = navToItem,
                            showRenameDialog = showRenameDialog,
                            showDownloadDialog = showDownloadDialog,
                            showDeletePlaylistDialog = showDeletePlaylistDialog,
                            uiState = uiState,
                            criticalError = criticalError,
                            innerPadding = innerPadding
                        )
                    })
            }
        )
    }

    if(showRenameDialog.value) {
        var newName by remember {
            mutableStateOf(uiState.title)
        }

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            title = { Text(text = stringResource(R.string.rename_playlist)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text(text = stringResource(R.string.new_name)) },
                    label = { Text(text = stringResource(R.string.new_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(FocusRequester()),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        showRenameDialog.value = false
                        renamePlaylist(newName)
                    })
                )
            },
            onDismissRequest = { showRenameDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    showRenameDialog.value = false
                    renamePlaylist(newName)
                }) {
                    Text(text = stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if(showDownloadDialog.value) {
        MultiDownloadDialog(
            onSave = {
                showDownloadDialog.value = false
                updateDownloads(it)
            },
            title = stringResource(R.string.download_playlist),
            text = stringResource(R.string.how_many_unwatched_items_do_you_want_to_keep_downloaded),
            currentDownloadCount = uiState.currentDownloadCount
        )
    }

    if(showDeletePlaylistDialog.value) {
        YesNoDialog(
            onNo = {
                showDeletePlaylistDialog.value = false
            },
            onYes = {
                showDeletePlaylistDialog.value = false
                deletePlaylist()
            },
            title = stringResource(R.string.confirm_delete),
            message = stringResource(R.string.are_you_sure_you_want_to_delete_this_playlist)
        )
    }

    if(uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = hideError, message = uiState.errorMessage)
    }
}


@Composable
private fun HorizontalTabletLayout(
    listUpdated: () -> Unit,
    updateListOrderOnServer: (Int, Int) -> Unit,
    playUpNext: () -> Unit,
    deleteItem: (Int) -> Unit,
    playItem: (Int) -> Unit,
    navToItem: (Int) -> Unit,
    showRenameDialog: MutableState<Boolean>,
    showDownloadDialog: MutableState<Boolean>,
    showDeletePlaylistDialog: MutableState<Boolean>,
    uiState: PlaylistDetailsUIState,
    criticalError: Boolean,
    innerPadding: PaddingValues
) {

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if(uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val data = remember {
        mutableStateOf(uiState.items)
    }

    if(uiState.updateList) {
        data.value = uiState.items
        listUpdated()
    }

    //There is 1 before the playlist items, so subtract 1 from indices
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            try{
                data.value = data.value.toMutableList().apply {
                    add(to.index - 1, removeAt(from.index - 1))
                }
            } catch(_: Throwable) { }
        },
        onDragEnd = { from, to ->
            updateListOrderOnServer(from - 1, to - 1)
        }
    )

    Row(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = 0.33f)
        ) {

            AsyncImage(
                model = uiState.posterUrl,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.DarkGray)
                    .blur(50.dp)
            )

            AsyncImage(
                model = uiState.posterUrl,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .reorderable(state),
            horizontalAlignment = columnAlignment,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (!uiState.loading && !criticalError) {
                item {
                    PlaybackLayout(
                        showRenameDialog = showRenameDialog,
                        playUpNext = playUpNext,
                        showDownloadDialog = showDownloadDialog,
                        uiState = uiState,
                        criticalError = criticalError
                    )
                }

                items(data.value, { it.id }) { playlistItem ->
                    ReorderableItem(state, key = playlistItem.id) { isDragging ->
                        PlaylistItemLayout(
                            deleteItem = deleteItem,
                            playItem = playItem,
                            navToItem = navToItem,
                            playlistItem = playlistItem,
                            isDragging = isDragging,
                            state = state
                        )
                    }
                }

                item {
                    DeleteLayout(
                        showDeletePlaylistDialog = showDeletePlaylistDialog,
                        uiState = uiState
                    )
                }

            }
        }
    }
}


@Composable
private fun PhoneLayout(
    listUpdated: () -> Unit,
    updateListOrderOnServer: (Int, Int) -> Unit,
    playUpNext: () -> Unit,
    deleteItem: (Int) -> Unit,
    playItem: (Int) -> Unit,
    navToItem: (Int) -> Unit,
    showRenameDialog: MutableState<Boolean>,
    showDownloadDialog: MutableState<Boolean>,
    showDeletePlaylistDialog: MutableState<Boolean>,
    uiState: PlaylistDetailsUIState,
    criticalError: Boolean,
    innerPadding: PaddingValues
) {

    val configuration = LocalConfiguration.current
    val hdp = configuration.screenWidthDp.dp * 0.5625f

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if (uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val data = remember {
        mutableStateOf(uiState.items)
    }

    if (uiState.updateList) {
        data.value = uiState.items
        listUpdated()
    }

    //There are 2 items before the playlist items, so subtract 2 from indices
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            try {
                data.value = data.value.toMutableList().apply {
                    add(to.index - 2, removeAt(from.index - 2))
                }
            } catch (_: Throwable) {
            }
        },
        onDragEnd = { from, to ->
            updateListOrderOnServer(from - 2, to - 2)
        }
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .padding(innerPadding)
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .reorderable(state),
            horizontalAlignment = columnAlignment,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hdp)
                ) {
                    AsyncImage(
                        model = uiState.posterUrl,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.DarkGray)
                            .blur(50.dp)
                    )

                    AsyncImage(
                        model = uiState.posterUrl,
                        contentDescription = "",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (!uiState.loading && !criticalError) {
                item {
                    PlaybackLayout(
                        showRenameDialog = showRenameDialog,
                        playUpNext = playUpNext,
                        showDownloadDialog = showDownloadDialog,
                        uiState = uiState,
                        criticalError = false
                    )
                }

                items(data.value, { it.id }) { playlistItem ->
                    ReorderableItem(state, key = playlistItem.id) { isDragging ->
                        PlaylistItemLayout(
                            deleteItem = deleteItem,
                            playItem = playItem,
                            navToItem = navToItem,
                            playlistItem = playlistItem,
                            isDragging = isDragging,
                            state = state
                        )
                    }
                }

                item {
                    DeleteLayout(
                        showDeletePlaylistDialog = showDeletePlaylistDialog,
                        uiState = uiState
                    )
                }

            }
        }

        if(uiState.loading || uiState.busy) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun PlaybackLayout(
    playUpNext: () -> Unit,
    showRenameDialog: MutableState<Boolean>,
    showDownloadDialog: MutableState<Boolean>,
    uiState: PlaylistDetailsUIState,
    criticalError: Boolean
) {

    val configuration = LocalConfiguration.current
    val alignment = if(configuration.isTablet()) Alignment.Start else Alignment.CenterHorizontally
    val modifier = if(configuration.isTablet()) Modifier.width(320.dp) else Modifier.fillMaxWidth()
    val buttonPadding = if(configuration.isTablet()) PaddingValues(0.dp, 0.dp  ) else PaddingValues(16.dp, 0.dp)

    if (uiState.loading) {
        Spacer(modifier = Modifier.height(48.dp))
        CircularProgressIndicator()
    } else  if (!criticalError) {

        val downloadIcon = when(uiState.downloadStatus) {
            DownloadStatus.None -> Icons.Filled.Download
            DownloadStatus.Finished -> Icons.Filled.DownloadDone
            else -> Icons.Filled.Downloading
        }
        val downloadText = when(uiState.downloadStatus) {
            DownloadStatus.None -> stringResource(R.string.download)
            DownloadStatus.Finished -> stringResource(R.string.downloaded)
            else -> stringResource(R.string.downloading)
        }


        Row(
            modifier = Modifier.padding(12.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = uiState.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { showRenameDialog.value = true }) {
                TintedIcon(imageVector = Icons.Filled.Edit)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(modifier = Modifier.padding(12.dp, 0.dp),
            text = stringResource(R.string.up_next, uiState.upNextTitle)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = alignment,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = playUpNext,
                modifier = modifier.padding(buttonPadding)
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Play,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if(uiState.partiallyPlayed) stringResource(R.string.resume) else stringResource(R.string.play)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showDownloadDialog.value = true },
                modifier = modifier.padding(buttonPadding)
            ) {
                Icon(
                    imageVector = downloadIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = downloadText)
            }

            Spacer(modifier = Modifier.height(16.dp))

        }

    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PlaylistItemLayout(
    deleteItem: (Int) -> Unit,
    playItem: (Int) -> Unit,
    navToItem: (Int) -> Unit,
    playlistItem: PlaylistItem,
    isDragging: Boolean,
    state: ReorderableLazyListState
) {

    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "")

    val dismissState = rememberDismissState(
        confirmStateChange = {
            if(it == DismissValue.DismissedToStart) {
                deleteItem(playlistItem.id)
                return@rememberDismissState true
            }
            false
        }
    )

    AnimatedVisibility(
        true, exit = fadeOut(spring())
    ) {
        SwipeToDismiss(
            state = dismissState,
            directions = setOf(DismissDirection.EndToStart),
            dismissThresholds = {
                FractionalThreshold(0.5f)
            },
            background = {
                val color = when (dismissState.dismissDirection) {
                    DismissDirection.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                }
                val direction = dismissState.dismissDirection

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color, shape = RoundedCornerShape(4.dp))
                        .padding(12.dp, 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (direction == DismissDirection.EndToStart) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            },
            dismissContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .height(64.dp)
                        .shadow(elevation.value)
                        .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp), shape = RoundedCornerShape(4.dp))
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(24.dp)
                            .clickable { }
                            .detectReorder(state)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DragIndicator,
                            contentDescription = "",
                            modifier = Modifier.size(24.dp, 64.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(114.dp)
                            .height(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = playlistItem.artworkUrl,
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color.DarkGray)
                                .clip(shape = RoundedCornerShape(4.dp))
                        )

                        TintedIcon(
                            imageVector = Icons.Filled.PlayCircleOutline,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(shape = CircleShape)
                                .background(color = Color.Black.copy(alpha = 0.5f))
                                .clickable { playItem(playlistItem.id) }
                        )

                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = playlistItem.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = playlistItem.description,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(64.dp)
                            .offset(x = (-12).dp),
                        contentAlignment = Alignment.Center
                    ) {

                        IconButton(onClick = { navToItem(playlistItem.id) }) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null
                            )
                        }
                    }

                }
            }
        )
    }


}

@Composable
private fun DeleteLayout(
    showDeletePlaylistDialog: MutableState<Boolean>,
    uiState: PlaylistDetailsUIState
) {

    val configuration = LocalConfiguration.current
    val modifier = if(configuration.screenWidthDp >= 352) Modifier.width(320.dp) else Modifier.fillMaxWidth()

    Box (
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                enabled = !(uiState.loading || uiState.busy),
                onClick = { showDeletePlaylistDialog.value = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = modifier
            ) {
                Text(text = stringResource(R.string.delete))
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}



@Preview
@Composable
private fun PlaylistDetailsScreenPreview() {
    val uiState = PlaylistDetailsUIState(
        loading = false,
        title = "My Playlist",
        canPlay = true,
        items = listOf(
            PlaylistItem(
                id = 1,
                index = 1,
                mediaId = 0,
                seriesId = 0,
                mediaType = MediaTypes.Movie,
                title = "Item 1",
                description = "Overview 1",
                artworkUrl = "",
                played = null,
                length = 5000.0,
                introStartTime = null,
                introEndTime = null,
                creditStartTime = null,
                bifUrl = "",
                videoUrl = "",
                externalSubtitles = listOf()
            ),
            PlaylistItem(
                id = 2,
                index = 2,
                mediaId = 0,
                seriesId = 0,
                mediaType = MediaTypes.Movie,
                title = "Item 2",
                description = "Overview 2",
                artworkUrl = "",
                played = null,
                length = 5000.0,
                introStartTime = null,
                introEndTime = null,
                creditStartTime = null,
                bifUrl = "",
                videoUrl = "",
                externalSubtitles = listOf()
            )
        )
    )

    DustyPigTheme(currentTheme = Themes.Maggies) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PlaylistDetailsScreenInternal(
                popBackStack = { },
                hideError = { },
                listUpdated = { },
                updateListOrderOnServer = { _, _ -> },
                playUpNext = { },
                deletePlaylist = { },
                deleteItem = { _ -> },
                playItem = { _ -> },
                navToItem = { _ -> },
                renamePlaylist = { _ -> },
                updateDownloads = { _ -> },
                uiState = uiState
            )
        }
    }
}
















