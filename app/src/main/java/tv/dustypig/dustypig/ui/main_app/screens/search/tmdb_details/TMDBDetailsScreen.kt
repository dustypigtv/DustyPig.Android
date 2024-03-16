package tv.dustypig.dustypig.ui.main_app.screens.search.tmdb_details

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.RequestStatus
import tv.dustypig.dustypig.api.models.TitleRequestPermissions
import tv.dustypig.dustypig.nav.MyRouteNavigator
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.composables.Avatar
import tv.dustypig.dustypig.ui.composables.BasicMediaView
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.Credits
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.isTablet

@Composable
fun TMDBDetailsScreen(vm: TMDBDetailsViewModel) {

    val uiState by vm.uiState.collectAsState()
    TMDBDetailsScreenInternal(
        uiState = uiState,
        routeNavigator = vm
    )
}

@Composable
private fun TMDBDetailsScreenInternal(
    uiState: TMDBDetailsUIState,
    routeNavigator: RouteNavigator
) {


    val showFriendsDialog = remember {
        mutableStateOf(false)
    }

    val criticalError by remember {
        derivedStateOf {
            uiState.showErrorDialog && uiState.criticalError
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                onClick = uiState.onPopBackStack,
                text = if(uiState.isMovie) stringResource(R.string.movie_info) else stringResource(R.string.series_info)
            )
        }
    ) { innerPadding ->

        OnDevice(
            onPhone = {
                PhoneLayout(
                    uiState = uiState,
                    criticalError = criticalError,
                    routeNavigator = routeNavigator,
                    showFriendsDialog = showFriendsDialog,
                    innerPadding = innerPadding
                )
            },
            onTablet = {
                OnOrientation(
                    onPortrait = {
                        PhoneLayout(
                            uiState = uiState,
                            criticalError = criticalError,
                            routeNavigator = routeNavigator,
                            showFriendsDialog = showFriendsDialog,
                            innerPadding = innerPadding
                        )
                    },
                    onLandscape = {
                        HorizontalTabletLayout(
                            uiState = uiState,
                            criticalError = criticalError,
                            routeNavigator = routeNavigator,
                            showFriendsDialog = showFriendsDialog,
                            innerPadding = innerPadding
                        )
                    })
            }
        )
    }

    if(showFriendsDialog.value) {
        var friendId by remember { mutableStateOf<Int?>(-1) }
        val titleType = if(uiState.isMovie) "movie" else "series"
        val listState = rememberLazyListState()

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = { showFriendsDialog.value = false },
            title = { Text(text = stringResource(R.string.request)) },
            text = {
                Column {
                    Text("Who do you want to request this $titleType from?")
                    Spacer(modifier = Modifier.height(24.dp))
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.friends) { friend ->

                            val backgroundColor = if(friendId == friend.id) MaterialTheme.colorScheme.primary else Color.Transparent
                            val textColor = if(friendId == friend.id) MaterialTheme.colorScheme.onPrimary else AlertDialogDefaults.textContentColor


                            Row (
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(color = backgroundColor, shape = RoundedCornerShape(48.dp))
                                    .clip(shape = RoundedCornerShape(size = 48.dp))
                                    .fillMaxSize()
                                    .clickable { friendId = friend.id }
                            ) {
                                Avatar(
                                    imageUrl = friend.avatarUrl,
                                    size=48,
                                    clickable = false
                                )
                                Text(
                                    text = friend.name,
                                    maxLines = 2,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = friendId != -1,
                    onClick = {
                        showFriendsDialog.value = false
                        uiState.onRequestTitle(friendId)
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFriendsDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if(uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = uiState.onHideError, message = uiState.errorMessage)
    }
}



@Composable
private fun HorizontalTabletLayout(
    uiState: TMDBDetailsUIState,
    criticalError: Boolean,
    routeNavigator: RouteNavigator,
    showFriendsDialog: MutableState<Boolean>,
    innerPadding: PaddingValues
) {

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if(uiState.loading) Alignment.CenterHorizontally else Alignment.Start

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

            AsyncImage(
                model = uiState.posterUrl,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                error = painterResource(id = R.drawable.error_tall)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = columnAlignment
        ) {
            InfoLayout(
                uiState = uiState,
                criticalError = criticalError,
                showFriendsDialog = showFriendsDialog,
                routeNavigator = routeNavigator
            )
        }
    }
}

@Composable
private fun PhoneLayout(
    uiState: TMDBDetailsUIState,
    criticalError: Boolean,
    routeNavigator: RouteNavigator,
    showFriendsDialog: MutableState<Boolean>,
    innerPadding: PaddingValues
) {

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

        InfoLayout(
            uiState = uiState,
            criticalError = criticalError,
            showFriendsDialog = showFriendsDialog,
            routeNavigator = routeNavigator
        )

    }
}



@Composable
fun InfoLayout(
    uiState: TMDBDetailsUIState,
    criticalError: Boolean,
    showFriendsDialog: MutableState<Boolean>,
    routeNavigator: RouteNavigator
) {

    if (uiState.loading) {
        Spacer(modifier = Modifier.height(48.dp))
        CircularProgressIndicator()
    } else if(!criticalError) {

        Column (
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {


            Text(
                text = uiState.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )


            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.year.isNotBlank()) {
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
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RectangleShape)
                            .padding(8.dp, 4.dp)
                    )
                }

            }

            Spacer(modifier = Modifier.height(12.dp))

            if(uiState.available.isEmpty()) {

                if(uiState.requestPermissions != TitleRequestPermissions.Disabled) {

                    val isTablet = LocalContext.current.isTablet()
                    val buttonModifier = remember {
                        if(isTablet) Modifier.width(320.dp) else Modifier.fillMaxWidth()
                    }

                    when(uiState.requestStatus) {
                        RequestStatus.NotRequested -> {
                            Button(
                                onClick = { showFriendsDialog.value = true },
                                modifier = buttonModifier,
                                enabled = !uiState.busy
                            ) {
                                if(uiState.busy)
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                else
                                    Text(text = stringResource(R.string.request))
                            }
                        }

                        RequestStatus.RequestSentToMain, RequestStatus.RequestSentToAccount -> {
                            Button(
                                onClick = uiState.onCancelRequest,
                                modifier = buttonModifier,
                                enabled = !uiState.busy
                            ) {
                                if(uiState.busy)
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                else
                                    Text(text = stringResource(R.string.cancel_request))
                            }
                        }

                        RequestStatus.Denied -> {
                            Text(text = if(uiState.isMovie) stringResource(R.string.your_request_for_this_movie_was_denied) else stringResource(R.string.your_request_for_this_series_was_denied))
                        }

                        RequestStatus.Pending -> {
                            Text(text = if(uiState.isMovie) stringResource(R.string.your_request_for_this_movie_was_accepted_and_is_pending) else stringResource(R.string.your_request_for_this_series_was_accepted_and_is_pending))
                        }

                        RequestStatus.Fulfilled -> {
                            Text(text = if(uiState.isMovie) stringResource(R.string.your_request_for_this_movie_was_completed) else stringResource(R.string.your_request_for_this_series_was_completed))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

            } else {

                Text(text = stringResource(R.string.available_on_dusty_pig))
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    state = rememberLazyListState(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    items(uiState.available) { basicMedia ->
                        BasicMediaView(
                            basicMedia = basicMedia,
                            routeNavigator = routeNavigator
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }


            Text(text = uiState.overview)
            Credits(uiState.creditsData)
        }
    }
}



@Preview
@Composable
private  fun TMDBDetailsScreenPreview() {

    val uiState = TMDBDetailsUIState(
        loading = false,
        isMovie = true,
        title = "The Avengers",
        year = "(2012)",
        rated = "PG-13",
        overview = "When an unexpected enemy emerges and threatens global safety and security, " +
                "Nick Fury, directory of the international peacekeeping agency known as S.H.I.E.L.D., " +
                "finds himself in need of a team to pull the world back from the brink of disaster. " +
                "Spanning the globe, a daring recruitment effort begins!",
        available = listOf(
            BasicMedia(
                id = 0,
                mediaType = MediaTypes.Movie,
                artworkUrl = "",
                backdropUrl = null,
                title = ""
            )
        )
    )

    PreviewBase {
        TMDBDetailsScreenInternal(
            uiState = uiState,
            routeNavigator = MyRouteNavigator()
        )
    }
}




















