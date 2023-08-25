package tv.dustypig.dustypig.ui.main_app.screens.series_details

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.ui.composables.Credits
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.TitleInfoData
import tv.dustypig.dustypig.ui.composables.TitleInfoLayout
import tv.dustypig.dustypig.ui.theme.DimOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailsScreen(vm: SeriesDetailsViewModel) {

    val uiState: SeriesDetailsUIState by vm.uiState.collectAsState()
    val titleInfoState: TitleInfoData by vm.titleInfoUIState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                        Text(
                            text = titleInfoState.title,
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
                    innerPadding = innerPadding
                )
            },
            onTablet = {
                OnOrientation(
                    onPortrait = {
                        PhoneLayout(
                            vm = vm,
                            innerPadding = innerPadding
                        )
                    },
                    onLandscape = {
                        HorizontalTabletLayout(
                            vm = vm,
                            innerPadding = innerPadding
                        )
                    })
            }
        )
    }


//    if(uiState.showRemoveDownload) {
//        YesNoDialog(
//            onNo = { vm.hideDownload(confirmed = false) },
//            onYes = { vm.hideDownload(confirmed = true) },
//            title = "Confirm",
//            message = "Do you want to remove the download?"
//        )
//    }

    if(uiState.showError) {
        ErrorDialog(onDismissRequest = { vm.hideError(uiState.criticalError) }, message = uiState.errorMessage)
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun EpisodeRow(episode: DetailedEpisode, vm: SeriesDetailsViewModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .height(64.dp)
            .background(color = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Box(
            modifier = Modifier
                .width(114.dp)
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            GlideImage(
                model = episode.artworkUrl,
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
                    .clickable { vm.playEpisode(episode.id) }
            )

        }


        Column(
            modifier = Modifier.weight(1f)
        ){
            Text(
                text = "E${episode.episodeNumber}: ${episode.title}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = episode.description ?: "No description",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Box(
            modifier = Modifier
                .width(24.dp)
                .height(64.dp)
                .offset(x = (-12).dp),
            contentAlignment = Alignment.Center
        ) {

            IconButton(onClick = { vm.navToEpisodeInfo(episode.id) }) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null
                )
            }
        }

    }
}



@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PhoneLayout(vm: SeriesDetailsViewModel, innerPadding: PaddingValues) {

    val uiState: SeriesDetailsUIState by vm.uiState.collectAsState()
    val titleInfoState: TitleInfoData by vm.titleInfoUIState.collectAsState()

    val configuration = LocalConfiguration.current
    val hdp = configuration.screenWidthDp.dp * 0.5625f

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if (uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val criticalError = remember {
        derivedStateOf {
            uiState.showError && uiState.criticalError
        }
    }

    LazyColumn(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth(),
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
                if (uiState.loading || uiState.backdropUrl.isBlank()) {
                    GlideImage(
                        model = uiState.posterUrl,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(50.dp)
                    )

                    if (uiState.backdropUrl.isBlank()) {
                        GlideImage(
                            model = uiState.posterUrl,
                            contentDescription = "",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    GlideImage(
                        model = uiState.backdropUrl,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (uiState.loading) {
                Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator()
            } else {

                TitleInfoLayout(titleInfoState)
            }

            Spacer(modifier = Modifier.height(24.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.seasons) {season ->
                    val seasonName = if(season == 0.toUShort()) "Specials" else "Season $season"
                    if(season == uiState.selectedSeason) {
                        Button(onClick = { /*Do nothing*/ }) {
                            Text(text = seasonName)
                        }
                    }
                    else {
                        OutlinedButton(onClick = { vm.setSeason(season) }) {
                            Text(text = seasonName)
                        }
                    }
                }
            }
        }


        items(uiState.episodes) { episode ->
            EpisodeRow(episode = episode, vm = vm)
        }

        item {
            Credits(uiState.creditsData)
        }

    }
}



@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HorizontalTabletLayout(vm: SeriesDetailsViewModel, innerPadding: PaddingValues) {

    val uiState: SeriesDetailsUIState by vm.uiState.collectAsState()
    val titleInfoState: TitleInfoData by vm.titleInfoUIState.collectAsState()

    val columnAlignment = if(uiState.loading) Alignment.CenterHorizontally else Alignment.Start


    val criticalError = remember {
        derivedStateOf {
            uiState.showError && uiState.criticalError
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues = innerPadding),
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

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = columnAlignment,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (uiState.loading) {
                    Spacer(modifier = Modifier.height(48.dp))
                    CircularProgressIndicator()
                } else if (!criticalError.value) {
                    TitleInfoLayout(titleInfoState)
                }
            }

            items(uiState.episodes) { episode ->
                EpisodeRow(episode = episode, vm = vm)
            }

            item {
                if (!uiState.loading && !criticalError.value) {
                    Credits(uiState.creditsData)
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

    }

}