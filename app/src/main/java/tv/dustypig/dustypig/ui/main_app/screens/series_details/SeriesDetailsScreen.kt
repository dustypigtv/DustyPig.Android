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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.Credits
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.composables.TitleInfoData
import tv.dustypig.dustypig.ui.composables.TitleInfoLayout

@Composable
fun SeriesDetailsScreen(vm: SeriesDetailsViewModel) {

    val uiState: SeriesDetailsUIState by vm.uiState.collectAsState()
    val titleInfoState: TitleInfoData by vm.titleInfoUIState.collectAsState()

    SeriesDetailsScreenInternal(
        popBackStack = vm::popBackStack,
        hideError = vm::hideError,
        playEpisode = vm::playEpisode,
        navToEpisodeInfo = vm::navToEpisodeInfo,
        selectSeason = vm::selectSeason,
        uiState = uiState,
        titleInfoState = titleInfoState
    )
}

@Composable
private fun SeriesDetailsScreenInternal(
    popBackStack: () -> Unit,
    hideError: () -> Unit,
    playEpisode: (Int) -> Unit,
    navToEpisodeInfo: (Int) -> Unit,
    selectSeason: (UShort) -> Unit,
    uiState: SeriesDetailsUIState,
    titleInfoState: TitleInfoData
) {


    var initialScrolled by remember {
        mutableStateOf(false)
    }
    val seasonsListState = rememberLazyListState()
    var selSeasonIdx = 0
    if(!(initialScrolled || uiState.loading)) {
        initialScrolled = false
        for (season in uiState.seasons) {
            if (season == uiState.upNextSeason)
                break
            selSeasonIdx++
        }
        LaunchedEffect(false){
            seasonsListState.scrollToItem(selSeasonIdx)
        }
    }


    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = popBackStack, text = stringResource(R.string.series_info))
        }
    ) { innerPadding ->

        OnDevice(
            onPhone = {
                PhoneLayout(
                    playEpisode = playEpisode,
                    navToEpisodeInfo = navToEpisodeInfo,
                    selectSeason = selectSeason,
                    innerPadding = innerPadding,
                    uiState = uiState,
                    titleInfoState = titleInfoState,
                    seasonsListState = seasonsListState
                )
            },
            onTablet = {
                OnOrientation(
                    onPortrait = {
                        PhoneLayout(
                            playEpisode = playEpisode,
                            navToEpisodeInfo = navToEpisodeInfo,
                            selectSeason = selectSeason,
                            innerPadding = innerPadding,
                            uiState = uiState,
                            titleInfoState = titleInfoState,
                            seasonsListState = seasonsListState
                        )
                    },
                    onLandscape = {
                        HorizontalTabletLayout(
                            playEpisode = playEpisode,
                            navToEpisodeInfo = navToEpisodeInfo,
                            selectSeason = selectSeason,
                            innerPadding = innerPadding,
                            uiState = uiState,
                            titleInfoState = titleInfoState,
                            seasonsListState = seasonsListState
                        )
                    })
            }
        )
    }

    if(uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = hideError, message = uiState.errorMessage)
    }
}


@Composable
private fun EpisodeRow(
    playEpisode: (Int) -> Unit,
    navToEpisodeInfo: (Int) -> Unit,
    episode: DetailedEpisode
) {
    
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
            AsyncImage(
                model = episode.artworkUrl,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.DarkGray)
                    .clip(shape = RoundedCornerShape(4.dp)),
                error = painterResource(id = R.drawable.error_wide)
            )

            TintedIcon(
                imageVector = Icons.Filled.PlayCircleOutline,
                modifier = Modifier
                    .size(36.dp)
                    .clip(shape = CircleShape)
                    .background(color = Color.Black.copy(alpha = 0.5f))
                    .clickable { playEpisode(episode.id) }
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

            IconButton(onClick = { navToEpisodeInfo(episode.id) }) {
                TintedIcon(
                    imageVector = Icons.Outlined.Info
                )
            }
        }

    }
}

@Composable
private fun PhoneLayout(
    playEpisode: (Int) -> Unit,
    navToEpisodeInfo: (Int) -> Unit,
    selectSeason: (UShort) -> Unit,
    innerPadding: PaddingValues,
    uiState: SeriesDetailsUIState,
    titleInfoState: TitleInfoData,
    seasonsListState: LazyListState
) {

    val configuration = LocalConfiguration.current
    val hdp = configuration.screenWidthDp.dp * 0.5625f

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if (uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val criticalError = uiState.showErrorDialog && uiState.criticalError



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
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(id = R.drawable.error_tall)
                    )
                } else {
                    AsyncImage(
                        model = uiState.backdropUrl,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.DarkGray),
                        error = painterResource(id = R.drawable.error_wide)
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
                            OutlinedButton(onClick = { selectSeason(season) }) {
                                Text(text = seasonName)
                            }
                        }
                    }
                }
            }

        }

        if (!uiState.loading && !criticalError) {
            items(uiState.episodes.filter { it.seasonNumber == uiState.selectedSeason }) { episode ->
                EpisodeRow(
                    playEpisode = playEpisode,
                    navToEpisodeInfo = navToEpisodeInfo,
                    episode = episode,
                )
            }
        }

        item {
            if (!criticalError) {
                Credits(uiState.creditsData)
            }
        }
    }
}

@Composable
private fun HorizontalTabletLayout(
    playEpisode: (Int) -> Unit,
    navToEpisodeInfo: (Int) -> Unit,
    selectSeason: (UShort) -> Unit,
    innerPadding: PaddingValues,
    uiState: SeriesDetailsUIState,
    titleInfoState: TitleInfoData,
    seasonsListState: LazyListState
) {

    val columnAlignment = if(uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val criticalError = uiState.showErrorDialog && uiState.criticalError


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
                modifier = Modifier.fillMaxSize(),
                error = painterResource(id = R.drawable.error_tall)
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
                            if (season == uiState.upNextSeason) {
                                Button(onClick = { /*Do nothing*/ }) {
                                    Text(text = seasonName)
                                }
                            } else {
                                OutlinedButton(onClick = { selectSeason(season) }) {
                                    Text(text = seasonName)
                                }
                            }
                        }
                    }
                }
            }

            if (!uiState.loading && !criticalError) {
                items(uiState.episodes.filter { it.seasonNumber == uiState.selectedSeason }) { episode ->
                    EpisodeRow(
                        playEpisode = playEpisode,
                        navToEpisodeInfo = navToEpisodeInfo,
                        episode = episode,
                    )
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


@Preview
@Composable
private fun SeriesDetailsScreenPreview() {

    val seasons = arrayListOf<UShort>()
    val episodes = arrayListOf<DetailedEpisode>()
    var idx = 0
    for(season in 1..7) {
        seasons.add(season.toUShort())
        for(episode in 1..23) {
            episodes.add(
                DetailedEpisode(
                    id = idx++,
                    bifUrl = null,
                    videoUrl = "",
                    externalSubtitles = null,
                    played = 0.0,
                    upNext = season == 2 && episode == 2,
                    title = "Episode $episode",
                    description = "Description $idx",
                    artworkUrl = "",
                    length = 1000.0,
                    introStartTime = null,
                    introEndTime = null,
                    creditStartTime = null,
                    seasonNumber = season.toUShort(),
                    episodeNumber = episode.toUShort(),
                    seriesId = 1,
                    seriesTitle = "My Series"
                )
            )
        }
    }

    val uiState = SeriesDetailsUIState(
        loading = false,
        seasons = seasons,
        upNextSeason = 2U,
        episodes = episodes
    )

    val titleInfoState = TitleInfoData(
        mediaType = MediaTypes.Series,
        title = "My Series",
        rated = "TV-Y7",
        length = "22m",
        overview = "Events Happen. People are affected. The story moves forward.",
        canManage = true,
        canPlay = true,
        partiallyPlayed = true,
        inWatchList = true
    )

    PreviewBase {
        SeriesDetailsScreenInternal(
            popBackStack = { },
            hideError = { },
            playEpisode = { _ -> },
            navToEpisodeInfo = { _ -> },
            selectSeason = { _ -> },
            uiState = uiState,
            titleInfoState = titleInfoState
        )
    }
}




























