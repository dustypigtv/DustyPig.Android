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
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import tv.dustypig.dustypig.api.models.PlaylistItem
import tv.dustypig.dustypig.download_manager.DownloadManager
import tv.dustypig.dustypig.download_manager.DownloadStatus
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.MultiDownloadDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.YesNoDialog
import tv.dustypig.dustypig.ui.isTablet
import tv.dustypig.dustypig.ui.theme.DimOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailsScreen(vm: PlaylistDetailsViewModel) {

    val uiState by vm.uiState.collectAsState()

    val criticalError by remember {
        derivedStateOf {
            uiState.showErrorDialog && uiState.criticalError
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Playlist Info",
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

        OnDevice(
            onPhone = {
                PhoneLayout(
                    vm = vm,
                    uiState = uiState,
                    criticalError = criticalError,
                    innerPadding = innerPadding
                )
            },
            onTablet = {
                OnOrientation(
                    onPortrait = {
                        PhoneLayout(
                            vm = vm,
                            uiState = uiState,
                            criticalError = criticalError,
                            innerPadding = innerPadding
                        )
                    },
                    onLandscape = {
                        HorizontalTabletLayout(
                            vm = vm,
                            uiState = uiState,
                            criticalError = criticalError,
                            innerPadding = innerPadding
                        )
                    })
            }
        )
    }

    if(uiState.showRenameDialog) {
        var newName by remember {
            mutableStateOf(uiState.title)
        }

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            title = { Text(text = "Rename Playlist") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text(text = "New Name") },
                    label = { Text(text = "New Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(FocusRequester()),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { vm.hideRenameDialog(confirmed = true, newName = newName) })
                )
            },
            onDismissRequest = { vm.hideRenameDialog(confirmed = false) },
            confirmButton = {
                TextButton(onClick = { vm.hideRenameDialog(confirmed = true, newName = newName) }) {
                    Text(text = "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.hideRenameDialog(confirmed = false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    if(uiState.showDownloadDialog) {
        MultiDownloadDialog(
            onSave = vm::hideDownloadDialog,
            title = "Download Playlist",
            itemName = "item",
            currentDownloadCount = uiState.currentDownloadCount
        )
    }

    if(uiState.showDeleteDialog) {
        YesNoDialog(
            onNo = { vm.hideDeletePlaylistDialog(false) },
            onYes = { vm.hideDeletePlaylistDialog(true) },
            title = "Confirm Delete",
            message = "Are you sure you want to delete this playlist?"
        )
    }

    if(uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = vm::hideError, message = uiState.errorMessage)
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HorizontalTabletLayout(vm: PlaylistDetailsViewModel, uiState: PlaylistDetailsUIState, criticalError: Boolean, innerPadding: PaddingValues) {

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if(uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val data = remember {
        mutableStateOf(uiState.items)
    }

    if(uiState.updateList) {
        data.value = uiState.items
        vm.listUpdated()
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
            vm.updateListOrderOnServer(from - 1, to - 1)
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

            GlideImage(
                model = uiState.posterUrl,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(50.dp)
            )

            GlideImage(
                model = uiState.posterUrl,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .reorderable(state),
            horizontalAlignment = columnAlignment,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                PlaybackLayout(vm = vm, uiState = uiState, criticalError = criticalError)
            }

            if (!uiState.loading && !criticalError) {
                items(data.value, { it.id }) { playlistItem ->
                    ReorderableItem(state, key = playlistItem.id) { isDragging ->
                        PlaylistItemLayout(playlistItem = playlistItem, vm = vm, isDragging = isDragging, state = state)
                    }
                }

                item {
                    DeleteLayout(vm = vm, uiState = uiState)
                }

            }
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PhoneLayout(vm: PlaylistDetailsViewModel, uiState: PlaylistDetailsUIState, criticalError: Boolean, innerPadding: PaddingValues) {

    val configuration = LocalConfiguration.current
    val hdp = configuration.screenWidthDp.dp * 0.5625f

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if (uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val data = remember {
        mutableStateOf(uiState.items)
    }

    if(uiState.updateList) {
        data.value = uiState.items
        vm.listUpdated()
    }

    //There are 2 items before the playlist items, so subtract 2 from indices
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            try{
                data.value = data.value.toMutableList().apply {
                    add(to.index - 2, removeAt(from.index - 2))
                }
            } catch(_: Throwable) { }
        },
        onDragEnd = { from, to ->
            vm.updateListOrderOnServer(from - 2, to - 2)
        }
    )

    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .padding(innerPadding)
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
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                GlideImage(
                    model = uiState.posterUrl,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(50.dp)
                )

                GlideImage(
                    model = uiState.posterUrl,
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        item {
            PlaybackLayout(vm = vm, uiState = uiState, criticalError = criticalError)
        }

        if (!uiState.loading && !criticalError) {
            items(data.value, { it.id }) { playlistItem ->
                ReorderableItem(state, key = playlistItem.id) { isDragging ->
                    PlaylistItemLayout(playlistItem = playlistItem, vm = vm, isDragging = isDragging, state = state)
                }
            }

            item {
                DeleteLayout(vm = vm, uiState = uiState)
            }

        }
    }
}

@Composable
private fun PlaybackLayout(vm: PlaylistDetailsViewModel, uiState: PlaylistDetailsUIState, criticalError: Boolean) {

    val configuration = LocalConfiguration.current
    val alignment = if(configuration.isTablet()) Alignment.Start else Alignment.CenterHorizontally
    val modifier = if(configuration.isTablet()) Modifier.width(320.dp) else Modifier.fillMaxWidth()
    val buttonPadding = if(configuration.isTablet()) PaddingValues(0.dp, 0.dp  ) else PaddingValues(16.dp, 0.dp)

    if (uiState.loading) {
        Spacer(modifier = Modifier.height(48.dp))
        CircularProgressIndicator()
    } else  if (!criticalError) {

        val status = DownloadManager
            .downloads
            .collectAsStateWithLifecycle(initialValue = listOf())
            .value
            .firstOrNull{ it.mediaId == uiState.playlistId }
            ?.status
        val downloadIcon = when(status) {
            DownloadStatus.Finished -> Icons.Filled.DownloadDone
            null -> Icons.Filled.Download
            else -> Icons.Filled.Downloading
        }
        val downloadText = when(status) {
            DownloadStatus.Finished -> "Downloaded"
            null -> "Download"
            else -> "Downloading"
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = uiState.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = vm::showRenameDialog) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Up Next: ${uiState.upNextTitle}")

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = alignment,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = vm::playUpNext,
                modifier = modifier.padding(buttonPadding)
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Play,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if(uiState.partiallyPlayed) "Resume" else "Play"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = vm::showDownloadDialog,
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
fun DismissBackground(dismissState: DismissState) {

    val color = when (dismissState.dismissDirection) {
        DismissDirection.EndToStart -> Color.Red
        else -> Color.Transparent
    }
    val direction = dismissState.dismissDirection

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        if (direction == DismissDirection.EndToStart) {
            Icon(
                // make sure add baseline_archive_24 resource to drawable folder
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete"
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterialApi::class)
@Composable
private fun PlaylistItemLayout(vm: PlaylistDetailsViewModel, playlistItem: PlaylistItem, isDragging: Boolean, state: ReorderableLazyListState) {

    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "")

    val dismissState = rememberDismissState(
        confirmStateChange = {
            if(it == DismissValue.DismissedToStart) {
                vm.deleteItem(playlistItem.id)
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
            modifier = Modifier,
            dismissThresholds = {
                FractionalThreshold(0.5f)
            },
            background = {
                DismissBackground(dismissState)
            },
            dismissContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .height(64.dp)
                        .background(color = MaterialTheme.colorScheme.tertiaryContainer)
                        .shadow(elevation.value)
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(24.dp)
                            .background(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        GlideImage(
                            model = playlistItem.artworkUrl,
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Icon(
                            imageVector = Icons.Filled.PlayCircleOutline,
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(shape = CircleShape)
                                .background(DimOverlay)
                                .clickable { vm.playItem(playlistItem.id) }
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
                            text = playlistItem.description ?: "No description",
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

                        IconButton(onClick = { vm.navToItem(playlistItem.id) }) {
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
private fun DeleteLayout(vm: PlaylistDetailsViewModel, uiState: PlaylistDetailsUIState) {

    val configuration = LocalConfiguration.current
    val modifier = if(configuration.isTablet()) Modifier.width(320.dp) else Modifier.fillMaxWidth()

    Spacer(modifier = Modifier.height(16.dp))

    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {

        if (uiState.deleteBusy) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = vm::deletePlaylist,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                modifier = modifier
            ) {
                Text(text = "Delete")
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

}