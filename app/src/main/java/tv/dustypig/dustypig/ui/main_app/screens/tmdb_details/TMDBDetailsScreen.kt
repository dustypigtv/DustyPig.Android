package tv.dustypig.dustypig.ui.main_app.screens.tmdb_details

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.api.models.RequestStatus
import tv.dustypig.dustypig.api.models.TitleRequestPermissions
import tv.dustypig.dustypig.ui.composables.Avatar
import tv.dustypig.dustypig.ui.composables.BasicMediaView
import tv.dustypig.dustypig.ui.composables.Credits
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.isTablet
import tv.dustypig.dustypig.ui.theme.DimOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TMDBDetailsScreen(vm: TMDBDetailsViewModel) {

    val uiState by vm.uiState.collectAsState()

    val criticalError by remember {
        derivedStateOf {
            uiState.showErrorDialog && uiState.criticalError
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if(uiState.isMovie) stringResource(R.string.movie_info) else stringResource(R.string.series_info) ,
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

    if(uiState.showFriendsDialog) {
        val friendId = remember { mutableIntStateOf(-1) }
        val titleType = if(uiState.isMovie) "movie" else "series"
        val listState = rememberLazyListState()

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = { vm.hideFriendsDialog(friendId = -1) },
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

                            val backgroundColor = if(friendId.intValue == friend.id) DimOverlay else Color.Transparent

                            Row (
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(backgroundColor, shape = RoundedCornerShape(size = 48.dp))
                                    .clip(shape = RoundedCornerShape(size = 48.dp))
                                    .fillMaxSize()
                                    .clickable { friendId.intValue = friend.id }
                            ) {
                                Avatar(
                                    basicProfile = BasicProfile(id = friend.id, name = friend.displayName, avatarUrl = friend.avatarUrl),
                                    modifier = Modifier
                                        .width(48.dp)
                                        .height(48.dp)
                                )
                                Text(
                                    text = friend.displayName,
                                    maxLines = 2,
                                    color = if(friendId.intValue == friend.id) MaterialTheme.colorScheme.primary else AlertDialogDefaults.textContentColor
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = friendId.intValue >= 0,
                    onClick = { vm.hideFriendsDialog(friendId = friendId.intValue) }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.hideFriendsDialog(friendId = -1) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if(uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = { vm.hideErrorDialog() }, message = uiState.errorMessage)
    }
}



@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HorizontalTabletLayout(vm: TMDBDetailsViewModel, uiState: TMDBDetailsUIState, criticalError: Boolean, innerPadding: PaddingValues) {

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = columnAlignment
        ) {
            InfoLayout(vm = vm, uiState = uiState, criticalError = criticalError)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PhoneLayout(vm: TMDBDetailsViewModel, uiState: TMDBDetailsUIState, criticalError: Boolean, innerPadding: PaddingValues) {

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
                .background(MaterialTheme.colorScheme.secondaryContainer)
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

        InfoLayout(vm = vm, uiState = uiState, criticalError = criticalError)

    }
}

@Composable
fun InfoLayout(vm: TMDBDetailsViewModel, uiState: TMDBDetailsUIState, criticalError: Boolean) {

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
                            .border(width = 1.dp, color = Color.White, shape = RectangleShape)
                            .padding(8.dp, 4.dp)
                    )
                }

            }

            Spacer(modifier = Modifier.height(12.dp))

            if(uiState.available.isEmpty()) {
                if(uiState.requestPermissions != TitleRequestPermissions.Disabled) {

                    val configuration = LocalConfiguration.current
                    val buttonModifier = remember {
                        if(configuration.isTablet()) Modifier.width(320.dp) else Modifier.fillMaxWidth()
                    }
                    val titleType = if(uiState.isMovie) "movie" else "series"

                    when(uiState.requestStatus) {
                        RequestStatus.NotRequested -> {
                            Button(
                                onClick = vm::requestTitle,
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
                                onClick = vm::cancelRequest,
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
                            vm
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