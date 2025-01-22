package tv.dustypig.dustypig.ui.main_app.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.HomeScreenList
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.nav.MyRouteNavigator
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.composables.BasicMediaView
import tv.dustypig.dustypig.ui.composables.PreviewBase


@Composable
fun HomeScreen(vm: HomeViewModel) {
    val uiState by vm.uiState.collectAsState()
    HomeScreenInternal(
        uiState = uiState,
        routeNavigator = vm
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreenInternal(
    uiState: HomeUIState,
    routeNavigator: RouteNavigator
) {

    //val showLoading = uiState.isFirstLoad && uiState.sections.isEmpty()
    val showEmpty = uiState.sections.isEmpty() && !uiState.isFirstLoad
    val showLoadingOrEmpty = uiState.isFirstLoad || showEmpty

    Box(modifier = Modifier.fillMaxSize()) {

        if (showLoadingOrEmpty) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(showEmpty) {
                    if(uiState.hasNetworkConnection) {
                        Text(text = stringResource(R.string.no_media_available))
                    } else {
                        Text(text = stringResource(R.string.no_internet_detected))
                    }
                } else {
                    Text(text = stringResource(R.string.loading))
                    Spacer(modifier = Modifier.height(12.dp))
                    CircularProgressIndicator()
                }
            }
        } else {
            val lazyColumnState = rememberLazyListState()
            val lazyRowStates = uiState.sections.associate { it.listId to rememberLazyListState() }

            LazyColumn(
                state = lazyColumnState,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.fillMaxSize()
            ) {

                items(uiState.sections, key = { section -> section.listId }) { section ->
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement()
                    ) {
                        Text(
                            text = section.title,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(6.dp, 2.dp)
                        )
                        LazyRow(
                            state = lazyRowStates[section.listId]!!,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .animateItemPlacement()
                        ) {
                            items(section.items, key = { basicMedia -> basicMedia.id }) { basicMedia ->

                                Box(modifier = Modifier.width(116.dp)) {
                                    BasicMediaView(
                                        basicMedia = basicMedia,
                                        routeNavigator = routeNavigator
                                    )
                                }
                            }

                            if(section.items.size >= 25) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .width(116.dp)
                                            .height(150.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .width(100.dp)
                                                .height(150.dp)
                                                .align(Alignment.Center)
                                                .background(
                                                    color = Color.DarkGray,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .clickable { uiState.onShowMoreClicked(section) }
                                        ){
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Icon(
                                                imageVector = Icons.Filled.KeyboardDoubleArrowRight,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(75.dp)
                                                    .align(Alignment.CenterHorizontally),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                modifier = Modifier
                                                    .height(75.dp)
                                                    .fillMaxWidth(),
                                                text = "Show All",
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

                item {
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
        }
    }
}


@Preview
@Composable
private fun HomeScreenPreview() {

    val items = arrayListOf<BasicMedia>()
    for(i in 1..25) {
        items.add(
            BasicMedia(
                id = i,
                mediaType = MediaTypes.Movie,
                artworkUrl = "",
                backdropUrl = null,
                title = ""
            )
        )
    }

    val sections = arrayListOf<HomeScreenList>()
    for(i in 1..40) {
        sections.add(
            HomeScreenList(
                listId = i.toLong(),
                title = "List $i",
                items = items
            )
        )
    }


    val uiState = HomeUIState(
        isFirstLoad = false,
        sections = sections
    )

    PreviewBase {
        HomeScreenInternal(
            uiState = uiState,
            routeNavigator = MyRouteNavigator()
        )
    }
}