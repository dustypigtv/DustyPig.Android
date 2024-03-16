package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.UserLock
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.CreditRoles
import tv.dustypig.dustypig.api.models.Genre
import tv.dustypig.dustypig.api.models.GenrePair
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.api.models.Person
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.ui.composables.ActionButton
import tv.dustypig.dustypig.ui.composables.CastTopAppBar
import tv.dustypig.dustypig.ui.composables.Credits
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.composables.YesNoDialog
import tv.dustypig.dustypig.ui.isTablet


@Composable
fun MovieDetailsScreen(vm: MovieDetailsViewModel) {
    val uiState by vm.uiState.collectAsState()
    MovieDetailsScreenInternal(uiState = uiState)
}


@Composable
private fun MovieDetailsScreenInternal(uiState: MovieDetailsUIState) {

    Scaffold(
        topBar = {
            CastTopAppBar(
                onClick = uiState.onPopBackStack,
                text = stringResource(R.string.movie_info),
                castManager = uiState.castManager
            )
        }
    ) { innerPadding ->

        OnDevice(
            onPhone = {
                PhoneLayout(
                    uiState = uiState,
                    innerPadding = innerPadding
                )
            },
            onTablet = {
                OnOrientation(
                    onPortrait = {
                        PhoneLayout(
                            uiState = uiState,
                            innerPadding = innerPadding
                        )
                    },
                    onLandscape = {
                        HorizontalTabletLayout(
                            uiState = uiState,
                            innerPadding = innerPadding
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
private fun HorizontalTabletLayout(
    uiState: MovieDetailsUIState,
    innerPadding: PaddingValues
) {

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if(uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val criticalError = uiState.showErrorDialog && uiState.criticalError

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = columnAlignment
        ) {

            if (uiState.loading) {
                Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator()
            } else if(!criticalError) {
                MovieTitleLayout(uiState = uiState)
            }
        }
    }
}

@Composable
private fun PhoneLayout(
    uiState: MovieDetailsUIState,
    innerPadding: PaddingValues
) {

    val configuration = LocalConfiguration.current
    val hdp = configuration.screenWidthDp.dp * 0.5625f

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if(uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val criticalError = uiState.showErrorDialog && uiState.criticalError

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

        if (uiState.loading) {
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator()
        } else if(!criticalError) {
            MovieTitleLayout(uiState = uiState)
        }
    }
}

@Composable
private fun MovieTitleLayout(uiState: MovieDetailsUIState) {

    //Align buttons to center for phone, left for tablet
    val context = LocalContext.current
    val isTablet = context.isTablet()
    val alignment = if(isTablet) Alignment.Start else Alignment.CenterHorizontally
    val modifier = if(isTablet) Modifier.width(320.dp) else Modifier.fillMaxWidth()
    val buttonPadding = if(isTablet) PaddingValues(0.dp, 0.dp  ) else PaddingValues(16.dp, 0.dp)

    val playButtonText =
        if (uiState.partiallyPlayed)
            stringResource(id = R.string.resume)
        else
            stringResource(R.string.play).trim()

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

    var showRemoveDownload by remember {
        mutableStateOf(false)
    }

    fun downloadClicked() {
        if (uiState.downloadStatus == DownloadStatus.None) {
            uiState.onAddDownload()
        } else {
            showRemoveDownload = true
        }
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
                if(uiState.year.isNotBlank()) {
                    Text(
                        text = uiState.year,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

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

                if (uiState.length.isNotBlank()) {
                    Text(
                        text = uiState.length,
                        style = MaterialTheme.typography.titleSmall
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
                        onClick = uiState.onToggleWatchlist,
                        caption = stringResource(R.string.watchlist),
                        icon = if (uiState.inWatchList) Icons.Filled.Check else Icons.Filled.Add
                    )
                }

                ActionButton(
                    onClick =  { downloadClicked() },
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
                            onClick = uiState.onMarkWatched,
                            caption = stringResource(R.string.mark_watched),
                            icon = Icons.Filled.RemoveRedEye
                        )
                    }
                }
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

    Text(
        text = uiState.overview,
        modifier = Modifier.padding(12.dp, 0.dp)
    )

    Credits(uiState.creditsData)


    if(showRemoveDownload) {
        YesNoDialog(
            onNo = { showRemoveDownload = false },
            onYes = {
                showRemoveDownload = false
                uiState.onRemoveDownload()
            },
            title = stringResource(R.string.confirm),
            message = stringResource(R.string.do_you_want_to_remove_the_download)
        )
    }

}

@Preview
@Composable
private fun MovieDetailsScreenPreview() {
    val uiState = MovieDetailsUIState(
        loading = false,
        title = "The Avengers",
        year = "(2012)",
        rated = "PG-13",
        length = "2h 23m",
        overview = "When an unexpected enemy emerges and threatens global safety and security, " +
                "Nick Fury, directory of the international peacekeeping agency known as S.H.I.E.L.D., " +
                "finds himself in need of a team to pull the world back from the brink of disaster. " +
                "Spanning the globe, a daring recruitment effort begins!",
        canManage = true,
        canPlay = true,
        partiallyPlayed = true,
        inWatchList = true,
        creditsData = CreditsData(
            genres = listOf(
                GenrePair.fromGenre(Genre.Action),
                GenrePair.fromGenre(Genre.Adventure),
                GenrePair.fromGenre(Genre.ScienceFiction)
            ),
            castAndCrew = listOf(
                Person(
                    id=3223,
                    name="Robert Downey Jr.",
                    initials = "RJ",
                    avatarUrl = "https://image.tmdb.org/t/p/original/im9SAqJPZKEbVZGmjXuLI4O7RvM.jpg",
                    order = 1,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=16828,
                    name="Chris Evans",
                    initials = "CE",
                    avatarUrl = "https://image.tmdb.org/t/p/original/3bOGNsHlrswhyW79uvIHH1V43JI.jpg",
                    order = 2,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=103,
                    name="Mark Ruffalo",
                    initials = "MR",
                    avatarUrl = "https://image.tmdb.org/t/p/original/5GilHMOt5PAQh6rlUKZzGmaKEI7.jpg",
                    order = 3,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=74568,
                    name="Chris Hemsworth",
                    initials = "CH",
                    avatarUrl = "https://image.tmdb.org/t/p/original/xkHHiJXraaMFXgRYspN6KVrFn17.jpg",
                    order = 4,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=1245,
                    name="Scarlett Johansson",
                    initials = "SJ",
                    avatarUrl = "https://image.tmdb.org/t/p/original/6NsMbJXRlDZuDzatN2akFdGuTvx.jpg",
                    order = 5,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=17604,
                    name="Jeremy Renner",
                    initials = "JR",
                    avatarUrl = "https://image.tmdb.org/t/p/original/yB84D1neTYXfWBaV0QOE9RF2VCu.jpg",
                    order = 6,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=91606,
                    name="Tom Hiddleston",
                    initials = "TH",
                    avatarUrl = "https://image.tmdb.org/t/p/original/mclHxMm8aPlCPKptP67257F5GPo.jpg",
                    order = 7,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=2231,
                    name="Samuel L. Jackson",
                    initials = "SJ",
                    avatarUrl = "https://image.tmdb.org/t/p/original/nCJJ3NVksYNxIzEHcyC1XziwPVj.jpg",
                    order = 8,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=71189,
                    name="Cobie Smulders",
                    initials = "CS",
                    avatarUrl = "https://image.tmdb.org/t/p/original/2CSgHUIrEi57pwtzdAY3HAxrp31.jpg",
                    order = 9,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=9048,
                    name="Clark Gregg",
                    initials = "CG",
                    avatarUrl = "https://image.tmdb.org/t/p/original/nbxFbr2SaF4Sdc6HdsF193GInvJ.jpg",
                    order = 10,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=1640,
                    name="Stellan Skarsgård",
                    initials = "SS",
                    avatarUrl = "https://image.tmdb.org/t/p/original/x78BtYHElirO7Iw8bL4m8CnzRDc.jpg",
                    order = 11,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=12052,
                    name="Gwyneth Paltrow",
                    initials = "GP",
                    avatarUrl = "https://image.tmdb.org/t/p/original/slPWN0VvYJtNOEuxlFSsXSNQMaF.jpg",
                    order = 12,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=6162,
                    name="Paul Bettany",
                    initials = "PB",
                    avatarUrl = "https://image.tmdb.org/t/p/original/vcAVrAOZrpqmi37qjFdztRAv1u9.jpg",
                    order = 13,
                    role = CreditRoles.Cast
                ),
                Person(
                    id=12891,
                    name="Joss Wheadon",
                    initials = "JW",
                    avatarUrl = "https://image.tmdb.org/t/p/original/mVvpZnKYKSCtkOQixQnOonV5kv3.jpg",
                    order = 1,
                    role = CreditRoles.Director
                ),
                Person(
                    id=10850,
                    name="Kevin Feige",
                    initials = "KF",
                    avatarUrl = "https://image.tmdb.org/t/p/original/kCBqXZ5PT5udYGEj2wfTSFbLMvT.jpg",
                    order = 1,
                    role = CreditRoles.Producer
                ),
                Person(
                    id=12891,
                    name="Joss Wheadon",
                    initials = "JW",
                    avatarUrl = "https://image.tmdb.org/t/p/original/mVvpZnKYKSCtkOQixQnOonV5kv3.jpg",
                    order = 1,
                    role = CreditRoles.Writer
                )
            )
        )
    )

    PreviewBase {
        MovieDetailsScreenInternal(uiState = uiState)
    }
}

























