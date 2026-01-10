package tv.dustypig.dustypig.ui.main_app.screens.playlist_details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import kotlinx.coroutines.job
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.PlaylistItem
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.ui.composables.CastTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.LazyColumnBottomAlign
import tv.dustypig.dustypig.ui.composables.MultiDownloadDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.composables.YesNoDialog
import tv.dustypig.dustypig.ui.isTablet
import tv.dustypig.dustypig.ui.theme.DarkRed


private val dismissPadding = 12.dp
private val itemHeight = 64.dp

@Composable
fun PlaylistDetailsScreen(vm: PlaylistDetailsViewModel) {
    val uiState by vm.uiState.collectAsState()
    PlaylistDetailsScreenInternal(uiState = uiState)
}

@Composable
private fun PlaylistDetailsScreenInternal(uiState: PlaylistDetailsUIState) {


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
            CastTopAppBar(
                onClick = uiState.onPopBackStack,
                text = stringResource(R.string.playlist_info),
                castManager = uiState.castManager
            )
        }
    ) { innerPadding ->

        OnDevice(
            onPhone = {
                PhoneLayout(
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

    if (showRenameDialog.value) {

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }
        var newName by remember {
            mutableStateOf(
                TextFieldValue(
                    text = uiState.title,
                    selection = TextRange(0, uiState.title.length)
                )
            )
        }

        LaunchedEffect(true) {
            this.coroutineContext.job.invokeOnCompletion {
                focusRequester.requestFocus()
            }
        }

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            title = { Text(text = stringResource(R.string.rename_playlist)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text(text = stringResource(R.string.new_name)) },
                    label = { Text(text = stringResource(R.string.new_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .width(300.dp)
                        .focusRequester(FocusRequester())
                )
            },
            onDismissRequest = { showRenameDialog.value = false },
            confirmButton = {
                TextButton(
                    enabled = newName.text.isNotEmpty() && newName.text != uiState.title,
                    onClick = {
                        keyboardController?.hide()
                        showRenameDialog.value = false
                        uiState.onRenamePlaylist(newName.text)
                    }
                ) {
                    Text(text = stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        showRenameDialog.value = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDownloadDialog.value) {
        MultiDownloadDialog(
            onSave = {
                showDownloadDialog.value = false
                uiState.onUpdateDownloads(it)
            },
            onDismiss = { showDownloadDialog.value = false },
            title = stringResource(R.string.download_playlist),
            text = stringResource(R.string.how_many_unwatched_items_do_you_want_to_keep_downloaded),
            currentDownloadCount = uiState.currentDownloadCount
        )
    }

    if (showDeletePlaylistDialog.value) {
        YesNoDialog(
            onNo = {
                showDeletePlaylistDialog.value = false
            },
            onYes = {
                showDeletePlaylistDialog.value = false
                uiState.onDeletePlaylist()
            },
            title = stringResource(R.string.confirm_delete),
            message = stringResource(R.string.are_you_sure_you_want_to_delete_this_playlist)
        )
    }

    if (uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = uiState.onHideError, message = uiState.errorMessage)
    }
}


@Composable
private fun HorizontalTabletLayout(
    showRenameDialog: MutableState<Boolean>,
    showDownloadDialog: MutableState<Boolean>,
    showDeletePlaylistDialog: MutableState<Boolean>,
    uiState: PlaylistDetailsUIState,
    criticalError: Boolean,
    innerPadding: PaddingValues
) {

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if (uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val data = remember {
        mutableStateOf(uiState.items)
    }

    if (uiState.updateList) {
        data.value = uiState.items
        uiState.onListUpdated()
    }



    //There is 1 before the playlist items, so subtract 1 from indices
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            try {
                data.value = data.value.toMutableList().apply {
                    add(to.index - 1, removeAt(from.index - 1))
                }
            } catch (_: Throwable) {
            }
        },
        onDragEnd = { from, to ->
            uiState.onUpdateListOnServer(from - 1, to - 1)
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
            )
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            LazyColumnBottomAlign(
                state = state.listState,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .reorderable(state),
                horizontalAlignment = columnAlignment,
            ) {

                if (!uiState.loading && !criticalError) {
                    item {
                        PlaybackLayout(
                            showRenameDialog = showRenameDialog,
                            playUpNext = uiState.onPlayUpNext,
                            showDownloadDialog = showDownloadDialog,
                            uiState = uiState
                        )
                    }

                    items(data.value, { it.id }) { playlistItem ->
                        ReorderableItem(state, key = playlistItem.id) { isDragging ->
                            PlaylistItemLayout(
                                playlistItem = playlistItem,
                                isDragging = isDragging,
                                reorderState = state,
                                uiState = uiState
                            )
                        }
                    }

                    item {
                        DeleteLayout(
                            showDeletePlaylistDialog,
                            uiState
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PhoneLayout(
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
        uiState.onListUpdated()
    }

    //There are 2 items before the playlist items, so subtract 2 from indices
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            try {
                data.value = data.value.toMutableList().apply {
                    add(to.index - 2, removeAt(from.index - 2))
                }
            } catch (_: Throwable) {
            }
        },
        onDragEnd = { from, to ->
            uiState.onUpdateListOnServer(from - 2, to - 2)
        }
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {

        LazyColumnBottomAlign(
            state = reorderState.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(reorderState),
            horizontalAlignment = columnAlignment
        ) {

           item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hdp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.backdropUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.default_playlist_backdrop),
                        error = painterResource(R.drawable.default_playlist_backdrop),
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.DarkGray)
                    )
                }
            }

            // While loading, the backdrop image is aligned to the bottom of the screen.
            // This fixes that
            item {
                Spacer(modifier = Modifier.height(0.dp))
            }

            if (!uiState.loading && !criticalError) {
                item {
                    PlaybackLayout(
                        showRenameDialog = showRenameDialog,
                        playUpNext = uiState.onPlayUpNext,
                        showDownloadDialog = showDownloadDialog,
                        uiState = uiState
                    )
                }

                items(data.value, { it.id }) { playlistItem ->
                    ReorderableItem(reorderState, key = playlistItem.id) { isDragging ->
                        PlaylistItemLayout(
                            playlistItem = playlistItem,
                            isDragging = isDragging,
                            reorderState = reorderState,
                            uiState = uiState
                        )
                    }
                }

                item {
                    DeleteLayout(
                        showDeletePlaylistDialog,
                        uiState
                    )
                }
            }
        }

        if (uiState.loading || uiState.busy) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun PlaybackLayout(
    playUpNext: () -> Unit,
    showRenameDialog: MutableState<Boolean>,
    showDownloadDialog: MutableState<Boolean>,
    uiState: PlaylistDetailsUIState
) {

    val isTablet = LocalContext.current.isTablet()

    //val configuration = LocalConfiguration.current
    val alignment = if (isTablet) Alignment.Start else Alignment.CenterHorizontally
    val modifier = if (isTablet) Modifier.width(320.dp) else Modifier.fillMaxWidth()
    val buttonPadding = if (isTablet) PaddingValues(0.dp, 0.dp) else PaddingValues(16.dp, 0.dp)

    if (uiState.loading) {
        Spacer(modifier = Modifier.height(48.dp))
        CircularProgressIndicator()
    } else {

        val downloadIcon = when (uiState.downloadStatus) {
            DownloadStatus.None -> Icons.Filled.Download
            DownloadStatus.Finished -> Icons.Filled.DownloadDone
            else -> Icons.Filled.HourglassTop
        }
        val downloadText = when (uiState.downloadStatus) {
            DownloadStatus.None -> stringResource(R.string.download)
            DownloadStatus.Finished -> stringResource(R.string.downloaded)
            else -> stringResource(R.string.downloading)
        }


        Row(
            modifier = Modifier.padding(12.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f).padding(0.dp)
            )

            IconButton(onClick = { showRenameDialog.value = true }) {
                TintedIcon(imageVector = Icons.Filled.Edit)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier.padding(12.dp, 0.dp),
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
                    text = if (uiState.partiallyPlayed) stringResource(R.string.resume) else stringResource(
                        R.string.play
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showDownloadDialog.value = true },
                modifier = modifier.padding(buttonPadding)
            ) {
                if(uiState.downloadStatus == DownloadStatus.Running) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        trackColor = MaterialTheme.colorScheme.tertiaryContainer,
                        progress = { uiState.downloadPercent }
                    )
                } else {
                    Icon(
                        imageVector = downloadIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = downloadText)
            }

            Spacer(modifier = Modifier.height(16.dp))

        }

    }
}


@Composable
private fun PlaylistItemCard(
    playlistItem: PlaylistItem,
    uiState: PlaylistDetailsUIState,
    elevationState: State<Dp>,
    reorderState: ReorderableLazyListState
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .height(itemHeight)
            .shadow(elevationState.value)
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                shape = RoundedCornerShape(4.dp)
            )
    ) {

        Box(
            modifier = Modifier
                .height(itemHeight)
                .width(24.dp)
                .clickable { }
                .detectReorder(reorderState)
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
                .height(itemHeight),
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
                    .clickable { uiState.onPlayItem(playlistItem.id) }
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

        Spacer(modifier = Modifier.width(18.dp))

        Box(
            modifier = Modifier
                .width(24.dp)
                .height(itemHeight)
                .offset(x = (-12).dp),
            contentAlignment = Alignment.Center
        ) {

            IconButton(onClick = { uiState.onNavToItem(playlistItem.id) }) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null
                )
            }
        }

    }
}


@Composable
private fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> DarkRed
        else -> Color.Transparent
    }

    val configuration = LocalConfiguration.current

    val boxWidth = configuration.screenWidthDp.dp - dismissPadding * 2
    val slide = boxWidth * dismissState.progress
    val xOffset = if (slide > 60.dp)
        min(dismissPadding * 4, (slide - dismissPadding * 3) / 2)
    else
        dismissPadding

    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(4.dp))
            .height(itemHeight)
            .fillMaxWidth()
            .background(color),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        if (dismissState.progress < 0.99f) {
            Icon(
                Icons.Outlined.Delete,
                tint = Color.White,
                contentDescription = "delete",
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = -xOffset)
            )
        }
    }
}


@Composable
private fun PlaylistItemLayout(
    playlistItem: PlaylistItem,
    isDragging: Boolean,
    reorderState: ReorderableLazyListState,
    uiState: PlaylistDetailsUIState
) {

    val delayTime = 300

    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "")
    var show by remember { mutableStateOf(true) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    show = false
                    uiState.onDeleteItem(playlistItem.id)
                    true
                }

                else -> false
            }
        },
        positionalThreshold = { it * 0.5f }
    )

    AnimatedVisibility(
        visible = show,
        exit = shrinkVertically(
            animationSpec = tween(
                durationMillis = delayTime,
            )
        )

    ) {
        SwipeToDismissBox(
            modifier = Modifier
                .padding(dismissPadding),
            state = dismissState,
            backgroundContent = { DismissBackground(dismissState) },
            enableDismissFromStartToEnd = false,
            content = {
                PlaylistItemCard(
                    playlistItem = playlistItem,
                    uiState = uiState,
                    elevationState = elevation,
                    reorderState = reorderState
                )
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
    val modifier =
        if (configuration.screenWidthDp >= 352) Modifier.width(320.dp) else Modifier.fillMaxWidth()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            enabled = !(uiState.loading || uiState.busy),
            onClick = { showDeletePlaylistDialog.value = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = modifier
        ) {
            Text(text = stringResource(R.string.delete_playlist))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}







@Preview
@Composable
private fun PlaylistDetailsScreenPreview() {

    val listItems: ArrayList<PlaylistItem> = arrayListOf()
    for (i in 1..2) {
        listItems.add(
            PlaylistItem(
                id = i,
                index = i,
                mediaId = 0,
                seriesId = 0,
                mediaType = MediaTypes.Movie,
                title = "Item $i",
                description = "Overview $i",
                artworkUrl = "",
                backdropUrl = "",
                length = 5000.0,
                introStartTime = null,
                introEndTime = null,
                creditsStartTime = null,
                bifUrl = "",
                videoUrl = ""
            )
        )
    }

    val uiState = PlaylistDetailsUIState(
        loading = false,
        title = "My Playlist",
        canPlay = true,
        items = listItems
    )

    PreviewBase {
        PlaylistDetailsScreenInternal(uiState = uiState)
    }
}















