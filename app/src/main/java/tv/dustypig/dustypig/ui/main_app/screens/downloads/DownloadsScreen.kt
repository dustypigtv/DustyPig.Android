package tv.dustypig.dustypig.ui.main_app.screens.downloads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.download_manager.UIDownload
import tv.dustypig.dustypig.global_managers.download_manager.UIJob
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.MultiDownloadDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.composables.YesNoDialog

@Composable
fun DownloadsScreen(vm: DownloadsViewModel) {
    val uiState by vm.uiState.collectAsState()
    DownloadsScreenInternal(uiState = uiState)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DownloadsScreenInternal(uiState: DownloadsUIState) {

    val delayTime = 300

    val listState = rememberLazyListState()

    var showEditDownloadDialog by remember {
        mutableStateOf(false)
    }

    var selectedJob by remember {
        mutableStateOf<UIJob?>(null)
    }

    var showDeleteAllDownloads by remember {
        mutableStateOf(false)
    }


    if(uiState.jobs.isEmpty()) {

        Box (
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text(text = "No Downloads")
        }

    } else {

        LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                state = listState
            ) {

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                for (job in uiState.jobs) {

                    item(key = job.key) {

                        val modifier = when (job.mediaType) {
                            MediaTypes.Series, MediaTypes.Playlist -> Modifier.clickable {
                                uiState.onToggleExpansion(job.mediaId)
                            }

                            else -> Modifier
                        }

                        var show by remember { mutableStateOf(true) }

                        val dismissState = rememberDismissState(
                            initialValue = DismissValue.Default,
                            confirmValueChange = {
                                when (it) {
                                    DismissValue.DismissedToStart -> {
                                        if(uiState.expandedMediaIds.contains(job.mediaId)) {
                                            uiState.onToggleExpansion(job.mediaId)
                                        }
                                        show = false
                                    }

                                    DismissValue.DismissedToEnd -> {
                                        selectedJob = job
                                        showEditDownloadDialog = true
                                    }

                                    else -> {}
                                }
                                false
                            },
                            positionalThreshold = { totalDistance -> (totalDistance * 0.5).dp.toPx() }
                        )

                        LaunchedEffect(show) {
                            if(!show) {
                                delay(delayTime.toLong())
                                uiState.onDeleteDownload(job)
                            }
                        }


                        val directions = when (job.mediaType) {
                            MediaTypes.Series, MediaTypes.Playlist -> setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd)
                            else -> setOf(DismissDirection.EndToStart)
                        }

                        AnimatedVisibility(
                            visible = show,
                            exit = shrinkVertically(
                                animationSpec = tween(
                                    durationMillis = delayTime,
                                )
                            )
                        ) {

                            SwipeToDismiss(
                                state = dismissState,
                                directions = directions,
                                background = {
                                    val color = when (dismissState.dismissDirection) {
                                        DismissDirection.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                        DismissDirection.StartToEnd -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> Color.Transparent
                                    }
                                    val direction = dismissState.dismissDirection

                                    Row(
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(8.dp))
                                            .fillMaxSize()
                                            .background(color, shape = RoundedCornerShape(8.dp))
                                            .padding(12.dp, 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = when (direction) {
                                            DismissDirection.EndToStart -> Arrangement.End
                                            DismissDirection.StartToEnd -> Arrangement.Start
                                            else -> {
                                                Arrangement.Center
                                            }
                                        }
                                    ) {
                                        when (direction) {
                                            DismissDirection.EndToStart -> Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onErrorContainer
                                            )

                                            DismissDirection.StartToEnd -> TintedIcon(
                                                imageVector = Icons.Filled.Edit
                                            )

                                            else -> {}
                                        }
                                    }
                                },
                                dismissContent = {
                                    Row(
                                        modifier = modifier
                                            .fillMaxWidth()
                                            .clip(shape = RoundedCornerShape(4.dp))
                                            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = RoundedCornerShape(4.dp))
                                            .animateItemPlacement(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {

                                        Box(
                                            modifier = Modifier
                                                .width(124.dp)
                                                .height(70.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (job.artworkPoster) {
                                                AsyncImage(
                                                    model = job.artworkUrl,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .blur(50.dp),
                                                    contentDescription = null,
                                                    error = painterResource(id = R.drawable.error_wide)
                                                )

                                                AsyncImage(
                                                    model = job.artworkUrl,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentDescription = null,
                                                    error = painterResource(id = R.drawable.error_wide)
                                                )
                                            } else {
                                                AsyncImage(
                                                    model = job.artworkUrl,
                                                    contentDescription = "",
                                                    error = painterResource(id = R.drawable.error_wide)
                                                )
                                            }

                                            var showPlay = job.status == DownloadStatus.Finished
                                            if (!showPlay) {
                                                if (job.mediaType == MediaTypes.Series || job.mediaType == MediaTypes.Playlist) {
                                                    showPlay = job.downloads.firstOrNull {
                                                        it.mediaId != job.mediaId
                                                    }?.status == DownloadStatus.Finished
                                                }
                                            }
                                            if (showPlay) {
                                                TintedIcon(
                                                    imageVector = Icons.Filled.PlayCircleOutline,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(shape = CircleShape)
                                                        .background(color = Color.Black.copy(alpha = 0.5f))
                                                        .clickable { uiState.onPlayNext(job) }
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(70.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.height(70.dp),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {

                                                Text(
                                                    text = job.title,
                                                    maxLines = when (job.status) {
                                                        DownloadStatus.Paused, DownloadStatus.Error -> 2
                                                        else -> 3
                                                    },
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.titleMedium
                                                )

                                                if (job.status == DownloadStatus.Paused)
                                                    Text(
                                                        text = job.statusDetails,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )

                                                if (job.status == DownloadStatus.Error)
                                                    Text(
                                                        text = job.statusDetails,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .padding(start = 0.dp, top = 0.dp, end = 8.dp, bottom = 0.dp)
                                                .height(70.dp),
                                            contentAlignment = Alignment.Center
                                        ) {

                                            when (job.status) {
                                                DownloadStatus.Finished -> {
                                                    TintedIcon(
                                                        imageVector = Icons.Filled.DownloadDone,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }

                                                DownloadStatus.Paused -> {
                                                    TintedIcon(
                                                        imageVector = Icons.Filled.Pause,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }

                                                DownloadStatus.Error -> {
                                                    Icon(
                                                        imageVector = Icons.Filled.Error,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(24.dp),
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }

                                                DownloadStatus.Pending -> {
                                                    TintedIcon(
                                                        imageVector = Icons.Filled.HourglassBottom,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }

                                                else -> {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        progress = 1.0f,
                                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                                    )

                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        progress = job.percent
                                                    )
                                                }
                                            }

                                        }

                                    }

                                }
                            )
                        }
                    }


                    if (uiState.expandedMediaIds.contains(job.mediaId)) {

                        for (dl in job.downloads.filter { it.mediaId != job.mediaId }) {
                            item(key = dl.key) {



                                Row(
                                    modifier = Modifier
                                        .padding(start = 36.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(4.dp))
                                        .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = RoundedCornerShape(4.dp))
                                        .animateItemPlacement(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .width(124.dp)
                                            .height(70.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dl.artworkPoster) {
                                            AsyncImage(
                                                model = dl.artworkUrl,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .blur(50.dp),
                                                contentDescription = null,
                                                error = painterResource(id = R.drawable.error_wide)
                                            )

                                            AsyncImage(
                                                model = dl.artworkUrl,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize(),
                                                contentDescription = null,
                                                error = painterResource(id = R.drawable.error_wide)
                                            )
                                        } else {
                                            AsyncImage(
                                                model = dl.artworkUrl,
                                                contentDescription = "",
                                                error = painterResource(id = R.drawable.error_wide)
                                            )
                                        }

                                        if (dl.status == DownloadStatus.Finished) {
                                            TintedIcon(
                                                imageVector = Icons.Filled.PlayCircleOutline,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(shape = CircleShape)
                                                    .background(color = Color.Black.copy(alpha = 0.5f))
                                                    .clickable { uiState.onPlayItem(job, dl) }
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(70.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.height(70.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = dl.title,
                                                maxLines = when (dl.status) {
                                                    DownloadStatus.Paused, DownloadStatus.Error -> 2
                                                    else -> 3
                                                },
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.titleMedium
                                            )

                                            if (dl.status == DownloadStatus.Paused)
                                                Text(
                                                    text = dl.statusDetails,
                                                    style = MaterialTheme.typography.bodySmall
                                                )

                                            if (dl.status == DownloadStatus.Error)
                                                Text(
                                                    text = dl.statusDetails,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .padding(start = 0.dp, top = 0.dp, end = 8.dp, bottom = 0.dp)
                                            .height(70.dp),
                                        contentAlignment = Alignment.Center
                                    ) {

                                        when (dl.status) {
                                            DownloadStatus.Finished -> {
                                                TintedIcon(
                                                    imageVector = Icons.Filled.DownloadDone,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            DownloadStatus.Paused -> {
                                                TintedIcon(
                                                    imageVector = Icons.Filled.Pause,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            DownloadStatus.Error -> {
                                                Icon(
                                                    imageVector = Icons.Filled.Error,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }

                                            DownloadStatus.Pending -> {
                                                TintedIcon(
                                                    imageVector = Icons.Filled.HourglassBottom,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            else -> {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    progress = 1.0f,
                                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                                )

                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    progress = dl.percent
                                                )
                                            }
                                        }

                                    }

                                }

                            }
                        }

                        if (job.downloads.any { it.mediaId != job.mediaId })
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                    }


                }

                item {

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.jobs.isNotEmpty()) {

                        val configuration = LocalConfiguration.current
                        val modifier = if (configuration.screenWidthDp >= 352) Modifier.width(320.dp) else Modifier.fillMaxWidth()

                        Box(
                            //modifier = Modifier.fillParentMaxHeight(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { showDeleteAllDownloads = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    modifier = modifier
                                ) {
                                    Text(text = stringResource(R.string.delete_all_downloads))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }


    }


    if(showEditDownloadDialog && selectedJob != null) {
        MultiDownloadDialog(
            onSave = { newCount ->
                showEditDownloadDialog = false
                uiState.onModifyDownload(selectedJob!!, newCount)
            },
            onDismiss = { showEditDownloadDialog = false },
            title = when(selectedJob!!.mediaType) {
                MediaTypes.Series -> stringResource(R.string.download_series)
                MediaTypes.Playlist -> stringResource(R.string.download_playlist)
                else -> ""
            },
            text = when(selectedJob!!.mediaType) {
                MediaTypes.Series -> stringResource(R.string.how_many_unwatched_episodes_do_you_want_to_keep_downloaded)
                MediaTypes.Playlist -> stringResource(R.string.how_many_unwatched_items_do_you_want_to_keep_downloaded)
                else -> ""
            },

            //selectedJob is remembered, which does not update the count property
            //easy fix
            currentDownloadCount = uiState.jobs.first { it.mediaId == selectedJob!!.mediaId }.count
        )
    }

    if(showDeleteAllDownloads) {
        YesNoDialog(
            onNo = { showDeleteAllDownloads = false },
            onYes = {
                showDeleteAllDownloads = false
                uiState.onDeleteAll()
            },
            title = stringResource(R.string.please_confirm),
            message = stringResource(R.string.are_you_sure_you_want_to_delete_all_downloads)
        )
    }

    if(uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = uiState.onHideError, message = uiState.errorMessage)
    }

}




@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun DownloadScreenPreview() {

    val uiState = DownloadsUIState(
        jobs = listOf(
            UIJob(
                key = "1",
                mediaId = 1,
                count = 1,
                mediaType = MediaTypes.Movie,
                title = "Big Buck Bunny (2008)",
                artworkUrl = "https://s3.dustypig.tv/demo-media/Movies/Big%20Buck%20Bunny%20%282008%29.backdrop.jpg",
                artworkPoster = false,
                percent = 1f,
                status = DownloadStatus.Finished,
                statusDetails = "",
                downloads = listOf()
            ),
            UIJob(
                key = "2",
                mediaId = 2,
                count = 1,
                mediaType = MediaTypes.Series,
                title = "Caminandes",
                artworkUrl = "https://s3.dustypig.tv/demo-media/TV%20Shows/Caminandes/backdrop.jpg",
                artworkPoster = false,
                percent = 0.66f,
                status = DownloadStatus.Running,
                statusDetails = "",
                downloads = listOf(
                    UIDownload(
                        key = "2.3",
                        mediaId = 3,
                        title = "s01e01 - Llama Drama",
                        artworkUrl = "https://s3.dustypig.tv/demo-media/TV%20Shows/Caminandes/Season%2001/Caminandes%20-%20s01e01%20-%20Llama%20Drama.jpg",
                        artworkPoster = false,
                        percent = 0.66f,
                        status = DownloadStatus.Running,
                        statusDetails = ""
                    )
                )
            )
        )
    )


    PreviewBase {
        DownloadsScreenInternal(uiState = uiState)
    }
}





















