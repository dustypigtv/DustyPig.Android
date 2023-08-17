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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import tv.dustypig.dustypig.ui.composables.Credits
import tv.dustypig.dustypig.ui.composables.OnDevice
import tv.dustypig.dustypig.ui.composables.OnOrientation
import tv.dustypig.dustypig.ui.composables.TitleInfoLayout

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun MovieDetailsScreen(vm: MovieDetailsViewModel) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {  },
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
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HorizontalTabletLayout(vm: MovieDetailsViewModel, innerPadding: PaddingValues) {

    val uiState: MovieDetailsUIState by vm.uiState.collectAsState()

    val configuration = LocalConfiguration.current
    val hdp = configuration.screenWidthDp.dp * 0.5625f

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
                .verticalScroll(rememberScrollState())
        ) {

            if (uiState.loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    CircularProgressIndicator()
                }
            } else {

                TitleInfoLayout(
                    playClick = { vm.play() },
                    toggleWatchList = { vm.toggleWatchList() },
                    download = { vm.download() },
                    addToPlaylist = { vm.addToPlaylist() },
                    markWatched = { vm.markWatched() },
                    requestAccess = { vm.requestAccess() },
                    manageClick = { vm.manageParentalControls() },
                    title = uiState.title,
                    year = uiState.year,
                    rated = uiState.rated,
                    length = uiState.length,
                    description = uiState.description,
                    canManage = uiState.canManage,
                    canPlay = uiState.canPlay,
                    partiallyPlayed = uiState.partiallyPlayed,
                    inWatchList = uiState.inWatchList
                )

                Credits(
                    genres = uiState.genres,
                    cast = uiState.cast,
                    directors = uiState.directors,
                    producers = uiState.producers,
                    writers = uiState.writers,
                    owner = uiState.owner
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PhoneLayout(vm: MovieDetailsViewModel, innerPadding: PaddingValues) {

    val uiState: MovieDetailsUIState by vm.uiState.collectAsState()

    val configuration = LocalConfiguration.current
    val hdp = configuration.screenWidthDp.dp * 0.5625f



    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally)
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator()
            }
        } else {

            TitleInfoLayout(
                playClick = { vm.play() },
                toggleWatchList = { vm.toggleWatchList() },
                download = { vm.download() },
                addToPlaylist = { vm.addToPlaylist() },
                markWatched = { vm.markWatched() },
                requestAccess = { vm.requestAccess() },
                manageClick = { vm.manageParentalControls() },
                title = uiState.title,
                year = uiState.year,
                rated = uiState.rated,
                length = uiState.length,
                description = uiState.description,
                canManage = uiState.canManage,
                canPlay = uiState.canPlay,
                partiallyPlayed = uiState.partiallyPlayed,
                inWatchList = uiState.inWatchList)

            Credits(
                genres = uiState.genres,
                cast = uiState.cast,
                directors = uiState.directors,
                producers = uiState.producers,
                writers = uiState.writers,
                owner = uiState.owner
            )
        }
    }
}

