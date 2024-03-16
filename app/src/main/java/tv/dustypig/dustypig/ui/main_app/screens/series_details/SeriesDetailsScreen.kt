package tv.dustypig.dustypig.ui.main_app.screens.series_details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.UserLock
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.Genre
import tv.dustypig.dustypig.api.models.GenrePair
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.ui.composables.ActionButton
import tv.dustypig.dustypig.ui.composables.CastTopAppBar
import tv.dustypig.dustypig.ui.composables.Credits
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.MultiDownloadDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.composables.YesNoDialog
import tv.dustypig.dustypig.ui.isTablet

@Composable
fun SeriesDetailsScreen(vm: SeriesDetailsViewModel) {
    val uiState: SeriesDetailsUIState by vm.uiState.collectAsState()
    SeriesDetailsScreenInternal(uiState = uiState)
}

@Composable
private fun SeriesDetailsScreenInternal(uiState: SeriesDetailsUIState) {

    Scaffold(
        topBar = {
            CastTopAppBar(
                onClick = uiState.onPopBackStack,
                text = stringResource(R.string.series_info),
                castManager = uiState.castManager
            )
        }
    ) { innerPadding ->

        OnDevice(
            onPhone = {
                PhoneLayout(
                    innerPadding = innerPadding,
                    uiState = uiState
                )
            },
            onTablet = {
                OnOrientation(
                    onPortrait = {
                        PhoneLayout(
                            innerPadding = innerPadding,
                            uiState = uiState
                        )
                    },
                    onLandscape = {
                        HorizontalTabletLayout(
                            innerPadding = innerPadding,
                            uiState = uiState
                        )
                    })
            }
        )
    }

    if(uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = uiState.onHideError, message = uiState.errorMessage)
    }
}


@Composable
private fun PhoneLayout(
    innerPadding: PaddingValues,
    uiState: SeriesDetailsUIState
) {

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
                    AsyncImage(
                        model = uiState.posterUrl,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.DarkGray)
                            .blur(50.dp)
                    )

                    //Prevents error flicker when navigating and no loading info was provided
                    if(uiState.posterUrl.isNotBlank()) {
                        AsyncImage(
                            model = uiState.posterUrl,
                            contentDescription = "",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                            error = painterResource(id = R.drawable.error_tall)
                        )
                    }
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

        }

        seriesLayout(uiState)
    }
}

@Composable
private fun HorizontalTabletLayout(
    innerPadding: PaddingValues,
    uiState: SeriesDetailsUIState
) {

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

            AsyncImage(
                model = uiState.posterUrl,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.DarkGray)
                    .blur(50.dp)
            )

            //Prevents error flicker when navigating and no loading info was provided
            if(uiState.posterUrl.isNotBlank()) {
                AsyncImage(
                    model = uiState.posterUrl,
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.error_tall)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = columnAlignment,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            seriesLayout(uiState)
        }
    }
}


private fun LazyListScope.seriesLayout(uiState: SeriesDetailsUIState) {

    val criticalError = uiState.showErrorDialog && uiState.criticalError

    item {
        if (uiState.loading) {
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator()
        } else if (!criticalError) {
            SeriesTitleLayout(uiState = uiState)
        }
    }

    if (!uiState.loading && !criticalError) {

        item {
            Spacer(modifier = Modifier.height(24.dp))
            SeasonsRow(uiState)
        }

        items(uiState.episodes.filter { it.seasonNumber == uiState.selectedSeason }) { episode ->
            EpisodeRow(
                uiState = uiState,
                episode = episode,
            )
        }

        item {
            Credits(uiState.creditsData)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
private fun SeriesTitleLayout(uiState: SeriesDetailsUIState) {

    //Align buttons to center for phone, left for tablet
    val context = LocalContext.current
    val isTablet = context.isTablet()
    val alignment = if(isTablet) Alignment.Start else Alignment.CenterHorizontally
    val modifier = if(isTablet) Modifier.width(320.dp) else Modifier.fillMaxWidth()
    val buttonPadding = if(isTablet) PaddingValues(0.dp, 0.dp  ) else PaddingValues(16.dp, 0.dp)

    val seasonEpisode =
        if(uiState.upNextSeason == null || uiState.upNextEpisode == null)
            ""
        else
            context.getString(R.string.season_episode, uiState.upNextSeason.toString(),  uiState.upNextEpisode.toString())

    val playButtonText =
        if (uiState.partiallyPlayed)
            stringResource(R.string.resume_season_episode, seasonEpisode).trim()
        else
            stringResource(R.string.play_season_episode, seasonEpisode).trim()


    //This will leave at minimum just the colon, so check if
    //length > 1 before displaying
    val epHeader = "${seasonEpisode}: ${uiState.episodeTitle}".trim()


    val downloadIcon = when(uiState.downloadStatus) {
        DownloadStatus.None -> Icons.Filled.Download
        DownloadStatus.Finished -> Icons.Filled.DownloadDone
        else -> Icons.Filled.Downloading
    }
    val downloadText = when(uiState.downloadStatus) {
        DownloadStatus.None -> stringResource(R.string.download)
        DownloadStatus.Finished -> stringResource(R.string.downloaded)
        else -> stringResource(R.string.downloading)
    }

    val subscribeIcon = if(uiState.subscribed)
        Icons.Filled.NotificationsOff
    else
        Icons.Filled.NotificationAdd

    val subscribeText = if(uiState.subscribed)
        stringResource(R.string.unsubscribe)
    else
        stringResource(R.string.subscribe)

    var showChangeDownloadCount by remember {
        mutableStateOf(false)
    }


    var showMarkWatchedDialog by remember {
        mutableStateOf(false)
    }


    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(8.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1F)
        ) {
            Text(
                text = uiState.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.rated.isNotBlank()) {
                    Text(
                        text = uiState.rated,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RectangleShape
                            )
                            .padding(8.dp, 4.dp)
                    )
                }
            }
        }

        if (uiState.canManage) {
            IconButton(onClick = uiState.onManagePermissions) {
                TintedIcon(
                    imageVector = FontAwesomeIcons.Solid.UserLock,
                    modifier = Modifier.size(20.dp)
                )
            }
        }


    }

    Spacer(modifier = Modifier.height(12.dp))

    if (uiState.canPlay) {
        Column(
            horizontalAlignment = alignment,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = uiState.onPlay,
                modifier = modifier.padding(buttonPadding)
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Play,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = playButtonText)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top,
                modifier = modifier.padding(0.dp, 12.dp)
            ) {


                if(uiState.watchListBusy) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(70.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(12.dp)
                        )
                        Text(
                            text = "Watchlist",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }

                } else {
                    ActionButton(
                        onClick = uiState.onToggleWatchList,
                        caption = stringResource(R.string.watchlist),
                        icon = if (uiState.inWatchList) Icons.Filled.Check else Icons.Filled.Add
                    )
                }

                ActionButton(
                    onClick =  { showChangeDownloadCount = true },
                    caption = downloadText,
                    icon = downloadIcon
                )

                ActionButton(
                    onClick = uiState.onAddToPlaylist,
                    caption = stringResource(R.string.add_to_playlist),
                    icon = Icons.Filled.PlaylistAdd
                )

                if(uiState.partiallyPlayed) {
                    if(uiState.markWatchedBusy) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(12.dp)
                            )
                            Text(
                                text = stringResource(R.string.mark_watched),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(58.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        ActionButton(
                            onClick = { showMarkWatchedDialog = true },
                            caption = stringResource(R.string.mark_watched),
                            icon = Icons.Filled.RemoveRedEye
                        )
                    }
                }

                ActionButton(
                    onClick = uiState.onToggleSubscribe,
                    caption = subscribeText,
                    icon = subscribeIcon
                )
            }
        }

    } else {

        val btnTxt = when (uiState.accessRequestStatus) {
            OverrideRequestStatus.NotRequested -> stringResource(R.string.request_access)
            OverrideRequestStatus.Requested -> stringResource(R.string.access_requested)
            OverrideRequestStatus.Denied -> stringResource(R.string.access_denied)
            OverrideRequestStatus.Granted -> stringResource(R.string.if_you_see_this_it_s_a_bug)
        }

        Column(
            horizontalAlignment = alignment,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.accessRequestBusy) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = uiState.onRequestAccess,
                    modifier = Modifier.padding(buttonPadding),
                    enabled = uiState.accessRequestStatus == OverrideRequestStatus.NotRequested
                ) {
                    Text(text = btnTxt)
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))


    if(epHeader.length > 1) {
        Text(
            text = epHeader,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.padding(12.dp, 0.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    Text(
        text = uiState.overview,
        modifier = Modifier.padding(12.dp, 0.dp)
    )


    if(showChangeDownloadCount) {
        MultiDownloadDialog(
            onSave = {
                showChangeDownloadCount = false
                uiState.onUpdateDownload(it)
            },
            onDismiss = { showChangeDownloadCount = false },
            title = stringResource(R.string.download_series),
            text = stringResource(R.string.how_many_unwatched_episodes_do_you_want_to_keep_downloaded),
            currentDownloadCount = uiState.currentDownloadCount
        )
    }

    if(showMarkWatchedDialog) {
        YesNoDialog(
            onNo = {
                showMarkWatchedDialog = false
                uiState.onMarkWatched(false)
            },
            onYes = {
                showMarkWatchedDialog = false
                uiState.onMarkWatched(true)
            },
            title = stringResource(R.string.mark_watched),
            message = stringResource(R.string.do_you_want_to_also_block_this_series_from_appearing_in_continue_watching)
        )
    }
}


@Composable
private fun SeasonsRow(uiState: SeriesDetailsUIState) {
    if (uiState.seasons.count() > 1) {

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

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
            state = seasonsListState
        ) {
            items(uiState.seasons) { season ->
                val seasonName =
                    if (season == 0.toUShort())
                        stringResource(R.string.specials)
                    else
                        stringResource(R.string.season, season)
                if (season == uiState.selectedSeason) {
                    Button(onClick = { /*Do nothing*/ }) {
                        Text(text = seasonName)
                    }
                } else {
                    OutlinedButton(onClick = { uiState.onSelectSeason(season) }) {
                        Text(text = seasonName)
                    }
                }
            }
        }
    }
}


@Composable
private fun EpisodeRow(
    uiState: SeriesDetailsUIState,
    episode: DetailedEpisode
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .height(64.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                shape = RoundedCornerShape(4.dp)
            )
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
                    .clickable { uiState.onPlayEpisode(episode.id) }
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

            IconButton(onClick = { uiState.onNavToEpisodeInfo(episode.id) }) {
                TintedIcon(
                    imageVector = Icons.Outlined.Info
                )
            }
        }

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
                    srtSubtitles = null,
                    played = 0.0,
                    upNext = season == 2 && episode == 2,
                    title = "Episode $episode",
                    description = "Description $idx",
                    artworkUrl = "",
                    length = 1000.0,
                    introStartTime = null,
                    introEndTime = null,
                    creditsStartTime = null,
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
        episodes = episodes,
        title = "My Series",
        rated = "TV-Y7",
        overview = "Events Happen. People are affected. The story moves forward.",
        canManage = true,
        canPlay = true,
        partiallyPlayed = true,
        inWatchList = true,
        creditsData = CreditsData(
            genres = listOf(
                GenrePair.fromGenre(Genre.Comedy)
            ),
            owner = "Jason"
        )
    )

    PreviewBase {
        SeriesDetailsScreenInternal(uiState = uiState)
    }
}

