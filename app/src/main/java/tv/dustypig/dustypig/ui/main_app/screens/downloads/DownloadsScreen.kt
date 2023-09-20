package tv.dustypig.dustypig.ui.main_app.screens.downloads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.download_manager.UIJob
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.MultiDownloadDialog
import tv.dustypig.dustypig.ui.composables.YesNoDialog
import tv.dustypig.dustypig.ui.theme.DimOverlay


@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun DownloadsScreen(vm: DownloadsViewModel) {

    val jobs by vm.downloadManager.downloads.collectAsState(initial = listOf())
    val uiState by vm.uiState.collectAsState()

    val expandedMediaIds = remember {
         mutableStateListOf<Int>()
    }

    var showRemoveDownloadDialog by remember {
        mutableStateOf(false)
    }

    var jobToRemove by remember {
        mutableStateOf<UIJob?>(null)
    }

    if(jobs.isEmpty()) {

        Box (
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text(text = "No Downloads")
        }

    } else {

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            for (job in jobs) {

                val modifier = when (job.mediaType) {
                    MediaTypes.Series, MediaTypes.Playlist -> Modifier.clickable {
                        if (expandedMediaIds.contains(job.mediaId)) {
                            expandedMediaIds.remove(job.mediaId)
                        } else {
                            expandedMediaIds.add(job.mediaId)
                        }
                    }

                    else -> Modifier
                }

                item(key = job.mediaId) {

                    val dismissState = rememberDismissState(
                        confirmStateChange = {
                            when(it) {
                                DismissValue.DismissedToStart -> {
                                    jobToRemove = job
                                    showRemoveDownloadDialog = true
                                }
                                DismissValue.DismissedToEnd -> {
                                    vm.showDownloadDialog(job.mediaId, job.mediaType)
                                }
                                else -> { }
                            }
                            false
                        }
                    )

                    val directions = when(job.mediaType) {
                        MediaTypes.Series, MediaTypes.Playlist -> setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd)
                        else -> setOf(DismissDirection.EndToStart)
                    }

                    AnimatedVisibility(
                        true, exit = fadeOut(spring())
                    ) {

                        SwipeToDismiss(
                            state = dismissState,
                            directions = directions,
                            dismissThresholds = {
                                FractionalThreshold(0.5f)
                            },
                            background = {
                                DismissBackground(dismissState, job)
                            },
                            dismissContent = {
                                Row(
                                    modifier = modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(8.dp))
                                        .background(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(8.dp))
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
                                            GlideImage(
                                                model = job.artworkUrl,
                                                contentDescription = "",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.onSecondary)
                                                    .blur(50.dp)
                                            )

                                            GlideImage(
                                                model = job.artworkUrl,
                                                contentDescription = "",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            GlideImage(
                                                model = job.artworkUrl,
                                                contentDescription = "",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.onSecondary)

                                            )
                                        }

                                        var showPlay = job.status == DownloadStatus.Finished
                                        if(!showPlay) {
                                            if(job.mediaType == MediaTypes.Series || job.mediaType == MediaTypes.Playlist)
                                                showPlay = job.downloads.firstOrNull()?.status == DownloadStatus.Finished
                                        }
                                        if(showPlay) {
                                            Icon(
                                                imageVector = Icons.Filled.PlayCircleOutline,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(shape = CircleShape)
                                                    .background(DimOverlay)
                                                    .clickable { vm.playNext(job.mediaId, job.mediaType) }
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
                                                maxLines = when(job.status) {
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
                                                    color = Color.Red
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
                                                Icon(
                                                    imageVector = Icons.Filled.DownloadDone,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            DownloadStatus.Paused -> {
                                                Icon(
                                                    imageVector = Icons.Filled.Pause,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            DownloadStatus.Error -> {
                                                Icon(
                                                    imageVector = Icons.Filled.Error,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                    tint = Color.Red
                                                )
                                            }
                                            DownloadStatus.Pending -> {
                                                Icon(
                                                    imageVector = Icons.Filled.HourglassBottom,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            else -> {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    color = MaterialTheme.colorScheme.onSecondary,
                                                    progress = 1.0f
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


                if (expandedMediaIds.contains(job.mediaId)) {

                    for (dl in job.downloads.filter { it.mediaId != job.mediaId }) {
                        item(key = dl.mediaId) {
                            Row(
                                modifier = Modifier
                                    .padding(start = 36.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(8.dp))
                                    .background(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(8.dp))
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
                                        GlideImage(
                                            model = dl.artworkUrl,
                                            contentDescription = "",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.onSecondary)
                                                .blur(50.dp)
                                        )

                                        GlideImage(
                                            model = dl.artworkUrl,
                                            contentDescription = "",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        GlideImage(
                                            model = dl.artworkUrl,
                                            contentDescription = "",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.onSecondary)

                                        )
                                    }

                                    if(dl.status == DownloadStatus.Finished) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayCircleOutline,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(shape = CircleShape)
                                                .background(DimOverlay)
                                                .clickable { vm.playItem(job.mediaId, job.mediaType, dl.mediaId) }
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
                                            maxLines = when(dl.status) {
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
                                                color = Color.Red
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
                                            Icon(
                                                imageVector = Icons.Filled.DownloadDone,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        DownloadStatus.Paused -> {
                                            Icon(
                                                imageVector = Icons.Filled.Pause,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        DownloadStatus.Error -> {
                                            Icon(
                                                imageVector = Icons.Filled.Error,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = Color.Red
                                            )
                                        }
                                        DownloadStatus.Pending -> {
                                            Icon(
                                                imageVector = Icons.Filled.HourglassBottom,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        else -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onSecondary,
                                                progress = 1.0f
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

                    if (job.downloads.filter { it.mediaId != job.mediaId }.isNotEmpty())
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                }


            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                if(jobs.isNotEmpty()) {

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
                                onClick = vm::deleteAll,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
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

    if(showRemoveDownloadDialog && jobToRemove != null) {
        YesNoDialog(
            onNo = {
                showRemoveDownloadDialog = false
            },
            onYes = {
                showRemoveDownloadDialog = false
                vm.removeDownload(jobToRemove!!)
            },
            title = stringResource(R.string.confirm),
            message = stringResource(R.string.do_you_want_to_remove_the_download)
        )
    }

    if(uiState.showDownloadDialog) {
        MultiDownloadDialog(
            onSave = { newCount ->
                vm.modifyDownload(newCount = newCount)
            },
            title = when(uiState.downloadDialogJobMediaType) {
                MediaTypes.Series -> stringResource(R.string.download_series)
                MediaTypes.Playlist -> stringResource(R.string.download_playlist)
                else -> ""
            },
            text = when(uiState.downloadDialogJobMediaType) {
                MediaTypes.Series -> stringResource(R.string.how_many_unwatched_episodes_do_you_want_to_keep_downloaded)
                MediaTypes.Playlist -> stringResource(R.string.how_many_unwatched_items_do_you_want_to_keep_downloaded)
                else -> ""
            },
            currentDownloadCount = uiState.downloadDialogCount
        )
    }

    if(uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = vm::hideError, message = uiState.errorMessage)
    }

}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DismissBackground(dismissState: DismissState, uiJob: UIJob) {

    val color = when (dismissState.dismissDirection) {
        DismissDirection.EndToStart -> Color.Red
        DismissDirection.StartToEnd -> when(uiJob.mediaType) {
            MediaTypes.Series -> MaterialTheme.colorScheme.onSecondary
            MediaTypes.Playlist -> MaterialTheme.colorScheme.onSecondary
            else -> Color.Transparent
        }
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
        horizontalArrangement = when(direction) {
            DismissDirection.EndToStart -> Arrangement.End
            DismissDirection.StartToEnd -> Arrangement.Start
            else -> { Arrangement.Center }
        }
    ) {
        when(direction) {
            DismissDirection.EndToStart -> Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete)
            )

            DismissDirection.StartToEnd -> Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.edit)
            )

            else -> { }
        }
    }
}