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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.Credits
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.MultiDownloadDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.composables.TitleInfoData
import tv.dustypig.dustypig.ui.composables.TitleInfoLayout
import tv.dustypig.dustypig.ui.composables.YesNoDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailsScreen(vm: SeriesDetailsViewModel) {

    val uiState: SeriesDetailsUIState by vm.uiState.collectAsState()
    val titleInfoState: TitleInfoData by vm.titleInfoUIState.collectAsState()

    val criticalError by remember {
        derivedStateOf {
            uiState.showErrorDialog && uiState.criticalError
        }
    }


    var initialScrolled by remember {
        mutableStateOf(false)
    }
    val seasonsListState = rememberLazyListState()
    var selSeasonIdx = 0
    if(!(initialScrolled || uiState.loading)) {
        initialScrolled = false
        for (season in uiState.seasons) {
            if (season == uiState.selectedSeason)
                break
            selSeasonIdx++
        }
        LaunchedEffect(false){
            seasonsListState.scrollToItem(selSeasonIdx)
        }
    }


    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = vm::popBackStack, text = stringResource(R.string.series_info))
        }
    ) { innerPadding ->

        OnDevice(
            onPhone = {
                PhoneLayout(
                    vm = vm,
                    innerPadding = innerPadding,
                    uiState = uiState,
                    titleInfoState = titleInfoState,
                    criticalError = criticalError,
                    seasonsListState = seasonsListState
                )
            },
            onTablet = {
                OnOrientation(
                    onPortrait = {
                        PhoneLayout(
                            vm = vm,
                            innerPadding = innerPadding,
                            uiState = uiState,
                            titleInfoState = titleInfoState,
                            criticalError = criticalError,
                            seasonsListState = seasonsListState
                        )
                    },
                    onLandscape = {
                        HorizontalTabletLayout(
                            vm = vm,
                            innerPadding = innerPadding,
                            uiState = uiState,
                            titleInfoState = titleInfoState,
                            criticalError = criticalError,
                            seasonsListState = seasonsListState
                        )
                    })
            }
        )
    }

    if(uiState.showMarkWatchedDialog) {
        YesNoDialog(
            onNo = { vm.hideMarkWatched(false) },
            onYes = { vm.hideMarkWatched(true) },
            title = stringResource(R.string.mark_watched),
            message = stringResource(R.string.do_you_want_to_also_block_this_series_from_appearing_in_continue_watching)
        )
    }

    if(uiState.showDownloadDialog) {
        MultiDownloadDialog(
            onSave = vm::hideDownloadDialog,
            title = stringResource(R.string.download_series),
            text = stringResource(R.string.how_many_unwatched_episodes_do_you_want_to_keep_downloaded),
            currentDownloadCount = uiState.currentDownloadCount
        )
    }

    if(uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = vm::hideError, message = uiState.errorMessage)
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
            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp), shape = RoundedCornerShape(4.dp))
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
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = RoundedCornerShape(4.dp))
            )

            TintedIcon(
                imageVector = Icons.Filled.PlayCircleOutline,
                modifier = Modifier
                    .size(36.dp)
                    .clip(shape = CircleShape)
                    .background(color = Color.Black.copy(alpha = 0.5f))
                    .clickable { vm.playEpisode(episode.id) }
            )

        }


        Column(
            modifier = Modifier.weight(1f)
        ){
            Text(
                text = episode.shortDisplayTitle(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = episode.description ?: stringResource(R.string.no_description),
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
                TintedIcon(
                    imageVector = Icons.Outlined.Info
                )
            }
        }

    }
}





@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PhoneLayout(vm: SeriesDetailsViewModel, innerPadding: PaddingValues, uiState: SeriesDetailsUIState, titleInfoState: TitleInfoData, criticalError: Boolean, seasonsListState: LazyListState) {

    val configuration = LocalConfiguration.current
    val hdp = configuration.screenWidthDp.dp * 0.5625f

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if (uiState.loading) Alignment.CenterHorizontally else Alignment.Start


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
            ) {
                if (uiState.backdropUrl.isBlank()) {
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
            } else  if (!criticalError) {
                TitleInfoLayout(titleInfoState)
            }


            Spacer(modifier = Modifier.height(24.dp))
            if(uiState.seasons.count() > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    state = seasonsListState
                ) {
                    items(uiState.seasons) { season ->
                        val seasonName = if (season == 0.toUShort()) stringResource(R.string.specials) else stringResource(R.string.season, season)
                        if (season == uiState.selectedSeason) {
                            Button(onClick = { /*Do nothing*/ }) {
                                Text(text = seasonName)
                            }
                        } else {
                            OutlinedButton(onClick = { vm.setSeason(season) }) {
                                Text(text = seasonName)
                            }
                        }
                    }
                }
            }

        }

        if (!uiState.loading && !criticalError) {
            items(uiState.episodes) { episode ->
                EpisodeRow(episode = episode, vm = vm)
            }
        }

        item {
            if (!criticalError) {
                Credits(uiState.creditsData)
            }
        }
    }
}



@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HorizontalTabletLayout(vm: SeriesDetailsViewModel, innerPadding: PaddingValues, uiState: SeriesDetailsUIState, titleInfoState: TitleInfoData, criticalError: Boolean, seasonsListState: LazyListState) {

    val columnAlignment = if(uiState.loading) Alignment.CenterHorizontally else Alignment.Start

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
                } else if (!criticalError) {
                    TitleInfoLayout(titleInfoState)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                if (uiState.seasons.count() > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        state = seasonsListState
                    ) {
                        items(uiState.seasons) { season ->
                            val seasonName = if (season == 0.toUShort()) stringResource(R.string.specials) else stringResource(R.string.season, season)
                            if (season == uiState.selectedSeason) {
                                Button(onClick = { /*Do nothing*/ }) {
                                    Text(text = seasonName)
                                }
                            } else {
                                OutlinedButton(onClick = { vm.setSeason(season) }) {
                                    Text(text = seasonName)
                                }
                            }
                        }
                    }
                }
            }

            if (!uiState.loading && !criticalError) {
                items(uiState.episodes) { episode ->
                    EpisodeRow(episode = episode, vm = vm)
                }
            }

            item {
                if (!uiState.loading && !criticalError) {
                    Credits(uiState.creditsData)
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

    }

}