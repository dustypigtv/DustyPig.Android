package tv.dustypig.dustypig.ui.main_app.screens.person_details

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicTMDB
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.TMDBMediaTypes
import tv.dustypig.dustypig.nav.MyRouteNavigator
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.composables.BasicMediaView
import tv.dustypig.dustypig.ui.composables.BasicTMDBView
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.CastTopAppBar as CastTopAppBar1


@Composable
fun PersonDetailsScreen(vm: PersonDetailsViewModel) {
    val uiState by vm.uiState.collectAsState()
    PersonDetailsScreenInternal(uiState = uiState, routeNavigator = vm)
}

@Composable
private fun PersonDetailsScreenInternal(
    uiState: PersonDetailsUIState,
    routeNavigator: RouteNavigator
) {

    Scaffold(
        topBar = {
            CastTopAppBar1(
                onClick = uiState.onPopBackStack,
                text = stringResource(R.string.person_details),
                castManager = uiState.castManager
            )
        }
    ) { innerPadding ->
        OnDevice(
            onPhone = {
                PhoneLayout(
                    uiState = uiState,
                    innerPadding = innerPadding,
                    routeNavigator = routeNavigator
                )
            },
            onTablet = {
                OnOrientation(
                    onPortrait = {
                        PhoneLayout(
                            uiState = uiState,
                            innerPadding = innerPadding,
                            routeNavigator = routeNavigator
                        )
                    },
                    onLandscape = {
                        HorizontalTabletLayout(
                            uiState = uiState,
                            innerPadding = innerPadding,
                            routeNavigator = routeNavigator
                        )
                    })
            }
        )
    }

    if (uiState.showErrorDialog) {
        ErrorDialog(onDismissRequest = uiState.onHideError, message = uiState.errorMessage)
    }
}


@Composable
private fun HorizontalTabletLayout(
    uiState: PersonDetailsUIState,
    routeNavigator: RouteNavigator,
    innerPadding: PaddingValues
) {
    //Left aligns content or center aligns busy indicator
    val columnAlignment = if (uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val criticalError = uiState.showErrorDialog && uiState.criticalError

    Row(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AsyncImage(
            model = uiState.avatarUrl,
            contentDescription = "",
            contentScale = ContentScale.Crop,
            placeholder = debugPlaceholder(R.drawable.grey_profile),
            error = painterResource(id = R.drawable.grey_profile),
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = 0.33f)
                .background(color = Color.DarkGray)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = columnAlignment
        ) {

            if (uiState.loading) {
                Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator()
            } else if (!criticalError) {
                InfoLayout(uiState, routeNavigator)
            }
        }
    }
}


@Composable
private fun PhoneLayout(
    uiState: PersonDetailsUIState,
    routeNavigator: RouteNavigator,
    innerPadding: PaddingValues
) {
    val configuration = LocalConfiguration.current
    val hdp = configuration.screenHeightDp.dp * 0.66f

    //Left aligns content or center aligns busy indicator
    val columnAlignment = if (uiState.loading) Alignment.CenterHorizontally else Alignment.Start

    val criticalError = uiState.showErrorDialog && uiState.criticalError

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = columnAlignment
    ) {
        AsyncImage(
            model = uiState.avatarUrl,
            contentDescription = "",
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.grey_profile),
            modifier = Modifier
                .fillMaxWidth()
                .height(hdp)
                .background(color = Color.DarkGray)
        )

        if (uiState.loading) {
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator()
        } else if (!criticalError) {
            InfoLayout(uiState = uiState, routeNavigator = routeNavigator)
        }
    }
}


@Composable
private fun InfoLayout(
    uiState: PersonDetailsUIState,
    routeNavigator: RouteNavigator
) {

    //Name
    Text(
        text = stringResource(R.string.person_name, uiState.name ?: ""),
        modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 0.dp)
    )

    //Available titles
    if (uiState.available.isNotEmpty()) {
        Text(
            text = stringResource(R.string.person_available_titles),
            modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 0.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 4.dp, 12.dp, 12.dp)
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            state = rememberLazyListState()
        ) {
            items(uiState.available) { basicMedia ->
                BasicMediaView(
                    basicMedia = basicMedia,
                    routeNavigator = routeNavigator
                )
            }
        }
    }

    //TMDB Titles
    if (uiState.otherTitles.isNotEmpty()) {
        Text(
            text = stringResource(R.string.person_other_titles),
            modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 0.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 4.dp, 12.dp, 12.dp)
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            state = rememberLazyListState()
        ) {
            items(uiState.otherTitles) { basicTMDB ->
                BasicTMDBView(
                    basicTMDB = basicTMDB,
                    routeNavigator = routeNavigator
                )
            }
        }
    }

    if (uiState.available.isNotEmpty() or uiState.otherTitles.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
    }


    //Other info
    if (uiState.birthday?.isNotBlank() == true) {
        if (uiState.placeOfBirth?.isNotBlank() == true) {
            Text(
                text = stringResource(R.string.person_born, uiState.birthday),
                modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 0.dp)
            )
            Text(
                text = uiState.placeOfBirth,
                modifier = Modifier.padding(12.dp, 0.dp, 12.dp, 0.dp)
            )
        }
    } else if (uiState.placeOfBirth?.isNotBlank() == true) {
        Text(
            text = stringResource(R.string.born_in, uiState.placeOfBirth),
            modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 0.dp)
        )
    }



    if (uiState.deathday?.isNotBlank() == true)
        Text(
            text = stringResource(R.string.person_died, uiState.deathday),
            modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 0.dp)
        )

    if (uiState.knownFor?.isNotBlank() == true)
        Text(
            text = stringResource(R.string.person_known_for, uiState.knownFor),
            modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 0.dp)
        )

    if (uiState.biography?.isNotBlank() == true) {
        Text(
            text = uiState.biography,
            modifier = Modifier.padding(12.dp, 23.dp, 12.dp, 0.dp)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
}


@Preview
@Composable
private fun PersonDetailsScreenPreview() {

    val available = arrayListOf<BasicMedia>()
    for (i in 1..25) {
        available.add(
            BasicMedia(
                id = i,
                mediaType = MediaTypes.Movie,
                artworkUrl = "",
                backdropUrl = null,
                title = ""
            )
        )
    }

    val tmdb = arrayListOf<BasicTMDB>()
    for (i in 1..25) {
        tmdb.add(
            BasicTMDB(
                tmdbId = i,
                mediaType = TMDBMediaTypes.Movie,
                artworkUrl = "",
                backdropUrl = null,
                title = ""
            )
        )
    }

    val uiState = PersonDetailsUIState(
        loading = false,
        name = "John Doe",
        birthday = "1/1/2000",
        placeOfBirth = "Somewhere, USA",
        deathday = "1/1/2100",
        knownFor = "Some cool movie",
        biography = "He was born. He was in some cool movie. Then he died.",
        available = available,
        otherTitles = tmdb
    )

    PreviewBase {
        PersonDetailsScreenInternal(uiState, MyRouteNavigator())
    }
}


@Composable
private fun debugPlaceholder(@DrawableRes debugPreview: Int) =
    if (LocalInspectionMode.current) {
        painterResource(id = debugPreview)
    } else {
        null
    }
