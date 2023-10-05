package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.settings_manager.Themes
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.Credits
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.TitleInfoData
import tv.dustypig.dustypig.ui.composables.TitleInfoLayout
import tv.dustypig.dustypig.ui.theme.DustyPigTheme


@Composable
fun MovieDetailsScreen(vm: MovieDetailsViewModel) {
    val uiState by vm.uiState.collectAsState()
    val titleInfoState by vm.titleInfoUIState.collectAsState()
    MovieDetailsScreenInternal(
        popBackStack = vm::popBackStack,
        hideError = vm::hideError,
        uiState = uiState,
        titleInfoState = titleInfoState
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieDetailsScreenInternal(
    popBackStack: () -> Unit,
    hideError: () -> Unit,
    uiState: MovieDetailsUIState,
    titleInfoState: TitleInfoData
) {

    val criticalError by remember {
        derivedStateOf {
            uiState.showErrorDialog && uiState.criticalError
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = popBackStack, text = stringResource(R.string.movie_info))
        }
    ) { innerPadding ->

        OnDevice(
            onPhone = {
                PhoneLayout(
                    uiState = uiState,
                    titleInfoState = titleInfoState,
                    criticalError = criticalError,
                    innerPadding = innerPadding
                )
            },
            onTablet = {
                OnOrientation(
                    onPortrait = {
                        PhoneLayout(
                            uiState = uiState,
                            titleInfoState = titleInfoState,
                            criticalError = criticalError,
                            innerPadding = innerPadding
                        )
                    },
                    onLandscape = {
                        HorizontalTabletLayout(
                            uiState = uiState,
                            titleInfoState = titleInfoState,
                            criticalError = criticalError,
                            innerPadding = innerPadding
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
private fun HorizontalTabletLayout(uiState: MovieDetailsUIState, titleInfoState: TitleInfoData, criticalError: Boolean, innerPadding: PaddingValues) {

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
                modifier = Modifier.fillMaxSize()
            )
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
                TitleInfoLayout(titleInfoState)
                Credits(uiState.creditsData)
            }
        }
    }
}

@Composable
private fun PhoneLayout(uiState: MovieDetailsUIState, titleInfoState: TitleInfoData, criticalError: Boolean, innerPadding: PaddingValues) {

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
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = uiState.backdropUrl,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.DarkGray)
                )
            }
        }

        if (uiState.loading) {
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator()
        } else if(!criticalError) {
            TitleInfoLayout(titleInfoState)
            Credits(uiState.creditsData)
        }
    }
}


@Preview
@Composable
private fun MovieDetailsScreenPreview() {
    val uiState = MovieDetailsUIState(
        loading = false,
        creditsData = CreditsData(
            genres = listOf("Action", "Adventure", "Science Fiction"),
            cast = listOf("Robert Downey Jr.", "Chris Evans", "Mark Ruffalo",
                "Chris Hemsworth", "Scarlett Johansson", "Jeremy Renner", "Tom Hiddleston",
                "Samuel L. Jackson", "Cobie Smulders", "Clark Gregg", "Stellan Skarsgard",
                "Gwyneth Paltrow", "Paul Bettany"),
            directors = listOf("Joss Wheadon"),
            producers = listOf("Kevin Feige"),
            writers = listOf("Joss Wheadon")
        )
    )
    val titleInfoState = TitleInfoData(
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
        inWatchList = true
    )
    DustyPigTheme(currentTheme = Themes.Maggies) {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MovieDetailsScreenInternal(
                popBackStack = { },
                hideError = { },
                uiState = uiState,
                titleInfoState = titleInfoState
            )
        }
    }
}


























