package tv.dustypig.dustypig.ui.main_app.screens.downloads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil.compose.AsyncImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.download_manager.UIDownload
import tv.dustypig.dustypig.global_managers.download_manager.UIJob
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.LazyColumnBottomAlign
import tv.dustypig.dustypig.ui.composables.MultiDownloadDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.composables.YesNoDialog
import tv.dustypig.dustypig.ui.theme.DarkGreen
import tv.dustypig.dustypig.ui.theme.DarkRed

private val dismissPadding = 12.dp

@Composable
fun DownloadsScreen(vm: DownloadsViewModel) {
    val uiState by vm.uiState.collectAsState()
    DownloadsScreenInternal(uiState = uiState)
}





@Composable
private fun DismissBackground(dismissState: SwipeToDismissBoxState) {

    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> DarkRed
        SwipeToDismissBoxValue.StartToEnd -> DarkGreen
        else -> Color.Transparent
    }

    val icon = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> Icons.Outlined.Delete
        SwipeToDismissBoxValue.StartToEnd -> Icons.Outlined.Edit
        else -> null
    }

    val arrangement = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> Arrangement.End
        else -> Arrangement.Start
    }

    val xOffsetMultiplier = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> -1f
        SwipeToDismissBoxValue.StartToEnd -> 1f
        else -> 0f
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
            .fillMaxSize()
            .background(color),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = arrangement
    ) {
        if (dismissState.progress < 0.99f) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    tint = Color.White,
                    contentDescription = "delete",
                    modifier = Modifier
                        .size(36.dp)
                        .offset(x = xOffset * xOffsetMultiplier)
                )
            }
        }
    }
}





@Composable
private fun Header(
    isPoster: Boolean,
    url: String,
    playButton: @Composable () -> Unit
) {

    Box(
        modifier = Modifier
            .width(124.dp)
            .height(70.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isPoster) {
            AsyncImage(
                model = url,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(50.dp),
                contentDescription = null
            )

            AsyncImage(
                model = url,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        } else {
            AsyncImage(
                model = url,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }

        playButton()
    }
}


@Composable
private fun JobInfo(job: UIJob) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Column(
            modifier = Modifier.height(70.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = job.title,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}





@Composable
private fun RowScope.DownloadInfo(download: UIDownload) {

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
                text = download.title,
                maxLines = when (download.status) {
                    DownloadStatus.Paused, DownloadStatus.Error -> 2
                    else -> 3
                },
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )

            if (download.status == DownloadStatus.Error)
                Text(
                    text = "Error",
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

        when (download.status) {
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
                    trackColor = MaterialTheme.colorScheme.tertiaryContainer,
                    progress = { download.percent }
                )
            }
        }
    }
}





@Composable
private fun DownloadCard(
    uiState: DownloadsUIState,
    job: UIJob,
    download: UIDownload?,
    modifier: Modifier
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(4.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                shape = RoundedCornerShape(4.dp)
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        if(download == null) {
            Header(job.artworkIsPoster, job.artworkUrl) { }
            JobInfo(job)
        } else {
            Header(download.artworkIsPoster, download.artworkUrl) {
                if (download.status == DownloadStatus.Finished) {
                    TintedIcon(
                        imageVector = Icons.Filled.PlayCircleOutline,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(shape = CircleShape)
                            .background(color = Color.Black.copy(alpha = 0.5f))
                            .clickable { uiState.onPlayItem(job, download) }
                    )
                }
            }
            DownloadInfo(download)
        }
    }
}






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


    if (uiState.jobs.isEmpty()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No Downloads",
                color = MaterialTheme.colorScheme.primary)
        }

    } else {


        LazyColumnBottomAlign (
            //verticalArrangement = Arrangement.spacedBy(12.dp),
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {

            item{
                Spacer(modifier = Modifier.height(12.dp))
            }

            for (job in uiState.jobs) {

                item(key = job.key) {

                    var show by remember { mutableStateOf(true) }

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {

                            when (it) {
                                SwipeToDismissBoxValue.EndToStart -> {
                                    show = false
                                    uiState.onDeleteDownload(job)
                                    true
                                }

                                SwipeToDismissBoxValue.StartToEnd -> {
                                    selectedJob = job
                                    showEditDownloadDialog = true
                                    false
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
                                .padding(dismissPadding, 0.dp),
                            state = dismissState,
                            enableDismissFromStartToEnd = when(job.mediaType) {
                                MediaTypes.Series, MediaTypes.Playlist -> true
                                else -> false
                            },
                            backgroundContent = { DismissBackground(dismissState) },
                            content = {
                                val modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                                when(job.mediaType) {
                                    MediaTypes.Series,
                                    MediaTypes.Playlist -> DownloadCard(uiState, job, null, modifier)
                                    else -> DownloadCard(uiState, job, job.downloads.first(), modifier)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                for (dl in job.downloads.filter { it.mediaId != job.mediaId }) {
                    item(key = dl.key) {
                        val modifier =  Modifier
                            .animateItem(fadeInSpec = null, fadeOutSpec = null)
                            .padding(36.dp, 0.dp, 12.dp, 0.dp)
                        DownloadCard(uiState, job, dl, modifier)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            item {

                if (uiState.jobs.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(16.dp))

                    val configuration = LocalConfiguration.current
                    val modifier =
                        if (configuration.screenWidthDp >= 352) Modifier.width(320.dp)
                        else Modifier.fillMaxWidth()


                    Box(
                        contentAlignment = Alignment.BottomCenter
                    ) {

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
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }


    if (showEditDownloadDialog && selectedJob != null) {
        MultiDownloadDialog(
            onSave = { newCount ->
                showEditDownloadDialog = false
                uiState.onModifyDownload(selectedJob!!, newCount)
            },
            onDismiss = { showEditDownloadDialog = false },
            title = when (selectedJob!!.mediaType) {
                MediaTypes.Series -> stringResource(R.string.download_series)
                MediaTypes.Playlist -> stringResource(R.string.download_playlist)
                else -> ""
            },
            text = when (selectedJob!!.mediaType) {
                MediaTypes.Series -> stringResource(R.string.how_many_unwatched_episodes_do_you_want_to_keep_downloaded)
                MediaTypes.Playlist -> stringResource(R.string.how_many_unwatched_items_do_you_want_to_keep_downloaded)
                else -> ""
            },

            //selectedJob is remembered, which does not update the count property
            //easy fix
            currentDownloadCount = uiState.jobs.first { it.mediaId == selectedJob!!.mediaId }.count
        )
    }

    if (showDeleteAllDownloads) {
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

    if (uiState.showErrorDialog) {
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
                artworkIsPoster = false,
                downloads = listOf()
            ),
            UIJob(
                key = "2",
                mediaId = 2,
                count = 1,
                mediaType = MediaTypes.Series,
                title = "Caminandes",
                artworkUrl = "https://s3.dustypig.tv/demo-media/TV%20Shows/Caminandes/backdrop.jpg",
                artworkIsPoster = false,
                downloads = listOf(
                    UIDownload(
                        key = "2.3",
                        mediaId = 3,
                        title = "s01e01 - Llama Drama",
                        artworkUrl = "https://s3.dustypig.tv/demo-media/TV%20Shows/Caminandes/Season%2001/Caminandes%20-%20s01e01%20-%20Llama%20Drama.jpg",
                        artworkIsPoster = false,
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





















