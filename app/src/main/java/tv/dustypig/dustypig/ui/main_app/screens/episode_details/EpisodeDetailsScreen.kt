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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import tv.dustypig.dustypig.download_manager.DownloadManager
import tv.dustypig.dustypig.download_manager.DownloadStatus
import tv.dustypig.dustypig.ui.composables.ActionButton
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.YesNoDialog
import tv.dustypig.dustypig.ui.isTablet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailsScreen (vm: EpisodeDetailsViewModel) {

    val uiState: EpisodeDetailsUIState by vm.uiState.collectAsState()

    val criticalError by remember {
        derivedStateOf {
            uiState.showError && uiState.criticalError
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Episode Info",
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

    if(uiState.showRemoveDownloadDialog) {
        YesNoDialog(
            onNo = { vm.hideDownload(confirmed = false) },
            onYes = { vm.hideDownload(confirmed = true) },
            title = "Confirm",
            message = "Do you want to remove the download?"
        )
    }

    if(uiState.showError) {
        ErrorDialog(onDismissRequest = { vm.hideError() }, message = uiState.errorMessage)
    }

}




@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HorizontalTabletLayout(vm: EpisodeDetailsViewModel, uiState: EpisodeDetailsUIState, criticalError: Boolean, innerPadding: PaddingValues){

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if(uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    Row(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        GlideImage(
            model = uiState.artworkUrl,
            contentDescription = "",
            contentScale = ContentScale.Fit,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxWidth(fraction = 0.33f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = columnAlignment,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            if (uiState.loading) {
                Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator()
            } else if(!criticalError) {
                InfoLayout(vm, uiState)
            }
        }
    }

}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PhoneLayout(vm: EpisodeDetailsViewModel, uiState: EpisodeDetailsUIState, criticalError: Boolean, innerPadding: PaddingValues){

    val configuration = LocalConfiguration.current
    val hdp = configuration.screenWidthDp.dp * 0.5625f

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if(uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = columnAlignment
    ){
        /**
         * Backdrop
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(hdp)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {

            GlideImage(
                model = uiState.artworkUrl,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (uiState.loading) {
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator()
        } else if(!criticalError) {
            Column (
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InfoLayout(vm, uiState)
            }
        }
    }
}


@Composable
private fun InfoLayout(vm: EpisodeDetailsViewModel, uiState: EpisodeDetailsUIState) {

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

    if(uiState.canPlay) {

        val configuration = LocalConfiguration.current
        val buttonPadding = if(configuration.isTablet()) PaddingValues(0.dp, 0.dp) else PaddingValues(16.dp, 0.dp)

        val status = DownloadManager
            .downloads
            .collectAsStateWithLifecycle(initialValue = listOf())
            .value
            .firstOrNull{ it.mediaId == uiState.mediaId }
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


        Button(
                onClick = { vm.play() },
                modifier = (if(configuration.isTablet()) Modifier.width(320.dp) else Modifier.fillMaxWidth())
                    .padding(buttonPadding)
            ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Play,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Play")
        }


        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(buttonPadding)
        ) {

            ActionButton(
                onClick = { vm.toggleDownload() },
                caption = downloadText,
                icon = downloadIcon
            )

            Spacer(modifier = Modifier.width(24.dp))

            ActionButton(
                onClick = { vm.addToPlaylist() },
                caption = "Add to Playlist",
                icon = Icons.Filled.PlaylistAdd
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    Text(text = uiState.overview)
    
    if(uiState.showGoToSeries) {
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = { vm.goToSeries() }) {
            Text(text = uiState.seriesTitle)
        }
    }
}



