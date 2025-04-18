package tv.dustypig.dustypig.ui.main_app.screens.episode_details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.ui.composables.CastTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OkDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.composables.YesNoDialog
import tv.dustypig.dustypig.ui.isTablet

@Composable
fun EpisodeDetailsScreen(vm: EpisodeDetailsViewModel) {
    val uiState: EpisodeDetailsUIState by vm.uiState.collectAsState()
    EpisodeDetailsScreenInternal(uiState = uiState)
}


@Composable
private fun EpisodeDetailsScreenInternal(uiState: EpisodeDetailsUIState) {

    val criticalError by remember {
        derivedStateOf {
            uiState.showErrorDialog && uiState.criticalError
        }
    }

    var showRemoveDownloadDialog by remember {
        mutableStateOf(false)
    }

    fun toggleDownload() {
        if (uiState.downloadStatus == DownloadStatus.None) {
            uiState.onAddDownload()
        } else {
            showRemoveDownloadDialog = true
        }
    }


    Scaffold(
        topBar = {
            CastTopAppBar(
                onClick = uiState.onPopBackStack,
                text = stringResource(R.string.episode_info),
                castManager = uiState.castManager
            )
        }
    ) { innerPadding ->

        OnDevice(
            onPhone = {
                PhoneLayout(
                    toggleDownload = ::toggleDownload,
                    uiState = uiState,
                    criticalError = criticalError,
                    innerPadding = innerPadding
                )
            },
            onTablet = {
                OnOrientation(
                    onPortrait = {
                        PhoneLayout(
                            toggleDownload = ::toggleDownload,
                            uiState = uiState,
                            criticalError = criticalError,
                            innerPadding = innerPadding
                        )
                    },
                    onLandscape = {
                        HorizontalTabletLayout(
                            toggleDownload = ::toggleDownload,
                            uiState = uiState,
                            criticalError = criticalError,
                            innerPadding = innerPadding
                        )
                    })
            }
        )
    }

    if (showRemoveDownloadDialog) {
        if(uiState.downloadingForPlaylistOrSeries) {
            OkDialog(
                onDismissRequest = { showRemoveDownloadDialog = false },
                title = stringResource(R.string.downloading_episode),
                message = stringResource(R.string.downloading_episode_for_series_or_playlist)
            )
        } else {
            YesNoDialog(
                onNo = { showRemoveDownloadDialog = false },
                onYes = {
                    showRemoveDownloadDialog = false
                    uiState.onRemoveDownload()
                },
                title = stringResource(R.string.confirm),
                message = stringResource(R.string.do_you_want_to_remove_the_download)
            )
        }
    }

    if (uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = uiState.onHideError, message = uiState.errorMessage)
    }

}


@Composable
private fun HorizontalTabletLayout(
    toggleDownload: () -> Unit,
    uiState: EpisodeDetailsUIState,
    criticalError: Boolean,
    innerPadding: PaddingValues
) {

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if (uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    Row(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AsyncImage(
            model = uiState.artworkUrl,
            contentDescription = "",
            contentScale = ContentScale.Fit,
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxWidth(fraction = 0.33f)
                .background(color = Color.DarkGray)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = columnAlignment,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            if (uiState.loading) {
                Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator()
            } else if (!criticalError) {
                InfoLayout(
                    toggleDownload = toggleDownload,
                    uiState = uiState
                )
            }
        }
    }

}


@Composable
private fun PhoneLayout(
    toggleDownload: () -> Unit,
    uiState: EpisodeDetailsUIState,
    criticalError: Boolean,
    innerPadding: PaddingValues
) {

    val configuration = LocalConfiguration.current
    val hdp = configuration.screenWidthDp.dp * 0.5625f

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if (uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = columnAlignment
    ) {
        /**
         * Backdrop
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(hdp)
        ) {

            AsyncImage(
                model = uiState.artworkUrl,
                contentDescription = null,
                modifier = Modifier
                    .background(color = Color.DarkGray)
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }


        if (uiState.loading) {
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator()
        } else if (!criticalError) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InfoLayout(
                    toggleDownload = toggleDownload,
                    uiState = uiState
                )
            }
        }
    }
}


@Composable
private fun InfoLayout(
    toggleDownload: () -> Unit,
    uiState: EpisodeDetailsUIState
) {

    Text(
        text = uiState.episodeTitle,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge,
    )
    if (uiState.length.isNotBlank()) {
        Text(
            text = uiState.length,
            style = MaterialTheme.typography.titleSmall
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (uiState.canPlay) {

        val isTablet = LocalContext.current.isTablet()
        val buttonPadding = if (isTablet) PaddingValues(0.dp, 0.dp) else PaddingValues(16.dp, 0.dp)

        val downloadIcon = when (uiState.downloadStatus) {
            DownloadStatus.None -> Icons.Filled.Download
            DownloadStatus.Finished -> Icons.Filled.DownloadDone
            else -> Icons.Filled.HourglassTop
        }

        Button(
            onClick = uiState.onPlay,
            modifier = (if (isTablet) Modifier.width(320.dp) else Modifier.fillMaxWidth())
                .padding(buttonPadding)
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Play,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(R.string.play))
        }


        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(buttonPadding)
                .fillMaxWidth()
        ) {

            if(!isTablet) {
                Spacer(modifier = Modifier.weight(1f))
            }

            IconButton(
                onClick = toggleDownload
            ) {
                if(uiState.downloadStatus == DownloadStatus.Running) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp),
                        trackColor = MaterialTheme.colorScheme.tertiaryContainer,
                        progress = { uiState.downloadPercent }
                    )
                } else {
                    TintedIcon(
                        imageVector = downloadIcon,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            IconButton(
                onClick = uiState.onAddToPlaylist
            ) {
                TintedIcon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    Text(text = uiState.overview)

    if (uiState.showGoToSeries) {
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = uiState.onGoToSeries) {
            Text(text = uiState.seriesTitle)
        }
    }
}


@Preview
@Composable
private fun EpisodeDetailsScreenPreview() {

    val uiState = EpisodeDetailsUIState(
        loading = false,
        episodeTitle = "s01e01 - Ep Title",
        overview = "This is the overview. Stuff happens in this episode. People are affected. The story is driven forward.",
        artworkUrl = "",
        canPlay = true,
        seriesTitle = "The Awesome Series",
        showGoToSeries = true,
        length = "42 m",
        downloadStatus = DownloadStatus.Running
    )

    PreviewBase {
        EpisodeDetailsScreenInternal(uiState = uiState)
    }
}
