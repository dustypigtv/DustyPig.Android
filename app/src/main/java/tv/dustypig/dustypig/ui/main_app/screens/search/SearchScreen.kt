package tv.dustypig.dustypig.ui.main_app.screens.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicTMDB
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.TMDBMediaTypes
import tv.dustypig.dustypig.global_managers.media_cache_manager.MediaCacheManager
import tv.dustypig.dustypig.nav.MyRouteNavigator
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.composables.BasicMediaView
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.main_app.screens.search.tmdb_details.TMDBDetailsNav


@Composable
fun SearchScreen(vm: SearchViewModel) {

    val uiState by vm.uiState.collectAsState()
    SearchScreenInternal(
        search = vm::search,
        updateQuery = vm::updateQuery,
        updateTabIndex = vm::updateTabIndex,
        uiState = uiState,
        routeNavigator = vm
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun SearchScreenInternal(
    search: () -> Unit,
    updateQuery: (String) -> Unit,
    updateTabIndex: (Int) -> Unit,
    uiState: SearchUIState,
    routeNavigator: RouteNavigator
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val availableState = rememberLazyGridState()
    val tmdbState = rememberLazyGridState()
    var active by remember { mutableStateOf(false) }


    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        SearchBar(
            query = uiState.query,
            onQueryChange = updateQuery,
            onSearch = {
                active = false
                search()
            },
            active = active,
            onActiveChange = { active = it },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                TintedIcon(imageVector = Icons.Filled.Search)
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if(uiState.query.isEmpty()) {
                            active = false
                        } else {
                            updateQuery("")
                            search()
                        }
                    }
                ) {
                    TintedIcon(imageVector = Icons.Filled.Cancel)
                }
            }
        ) {
            uiState.history.forEach { history ->
                Row(
                    modifier = Modifier
                        .clickable {
                            updateQuery(history)
                            active = false
                            search()
                        }
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TintedIcon(imageVector = Icons.Filled.History)
                    Text(text = history)
                }
            }
        }


        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            if (uiState.emptyQuery) {
                Text(text = stringResource(R.string.enter_search))

            } else if (uiState.hasResults) {

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                ) {

                    if (uiState.allowTMDB) {


                        TabRow(
                            selectedTabIndex = uiState.tabIndex
                        ) {
                            Tab(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo_transparent),
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color.Transparent),
                                            contentDescription = null
                                        )
                                        Text(text = stringResource(R.string.available))
                                    }
                                },
                                selected = uiState.tabIndex == 0,
                                onClick = {
                                    updateTabIndex(0)
                                    keyboardController?.hide()
                                },
                            )
                            Tab(
                                text = {
                                    Image(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.tmdb),
                                        modifier = Modifier.width(128.dp),
                                        contentDescription = null
                                    )
                                },
                                selected = uiState.tabIndex == 1,
                                onClick = {
                                    updateTabIndex(1)
                                    keyboardController?.hide()
                                },
                            )
                        }

                        when (uiState.tabIndex) {
                            0 -> AvailableLayout(uiState = uiState, listState = availableState, routeNavigator = routeNavigator)
                            1 -> TMDBLayout(uiState = uiState, listState = tmdbState, routeNavigator = routeNavigator)
                        }

                    } else {
                        AvailableLayout(uiState = uiState, listState = availableState, routeNavigator = routeNavigator)
                    }
                }


            } else {
                if(!uiState.progressOnly)
                    Text(text = stringResource(R.string.no_results))
            }

            if(uiState.busy)
                CircularProgressIndicator(modifier = Modifier.offset(x = 0.dp, y = (-48).dp))
        }

    }
}



@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AvailableLayout(uiState: SearchUIState, listState: LazyGridState, routeNavigator: RouteNavigator) {

    val keyboardController = LocalSoftwareKeyboardController.current

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        columns = GridCells.Adaptive(minSize = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        contentPadding = PaddingValues(12.dp)
    ) {

        items(
            uiState.availableItems.count()
        ) { index ->
            BasicMediaView(
                basicMedia = uiState.availableItems[index],
                routeNavigator = routeNavigator,
                clicked = { keyboardController?.hide() }
            )
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TMDBLayout(uiState: SearchUIState, listState: LazyGridState, routeNavigator: RouteNavigator) {

    val keyboardController = LocalSoftwareKeyboardController.current

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        columns = GridCells.Adaptive(minSize = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        contentPadding = PaddingValues(12.dp)
    ) {

        items(
            uiState.tmdbItems.count()
        ) { index ->
            TMDBMediaView(
                basicTMDB = uiState.tmdbItems[index],
                routeNavigator = routeNavigator,
                clicked = { keyboardController?.hide() }
            )
        }

    }
}



@Composable
fun TMDBMediaView(
    basicTMDB: BasicTMDB,
    routeNavigator: RouteNavigator,
    clicked: ((Int) -> Unit)? = null
) {
    fun onClicked() {

        if(clicked != null)
            clicked(basicTMDB.tmdbId)


        val cacheId = MediaCacheManager.add(basicTMDB)
        routeNavigator.navigateToRoute(
            route = TMDBDetailsNav.getRoute(
                basicTMDB.tmdbId,
                cacheId = cacheId,
                isMovie = basicTMDB.mediaType == TMDBMediaTypes.Movie
            )
        )
    }

    val wdp = 100.dp
    val hdp = 150.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(hdp)
    ) {
        AsyncImage(
            model = basicTMDB.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .background(color = Color.DarkGray)
                .align(Alignment.Center)
                .size(wdp, hdp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onClicked() },
            error = painterResource(id = R.drawable.error_tall)
        )
    }
}



@Preview
@Composable
private fun SearchScreenPreview() {

    val basicMediaList = arrayListOf<BasicMedia>()
    for(i in 1..10) {
        basicMediaList.add(
            BasicMedia(
                id = i,
                mediaType = MediaTypes.Movie,
                artworkUrl = "",
                backdropUrl = "",
                title = ""
            )
        )
    }

    val basicTMDBList = arrayListOf<BasicTMDB>()
    for(i in 1..10) {
        basicTMDBList.add(
            BasicTMDB(
                tmdbId = i,
                mediaType = TMDBMediaTypes.Movie,
                artworkUrl = "",
                backdropUrl = "",
                title = ""
            )
        )
    }




    val uiState = SearchUIState(
        busy = false,
        emptyQuery = false,
        hasResults = true,
        allowTMDB = true,
        availableItems = basicMediaList,
        tmdbItems = basicTMDBList
    )

    PreviewBase {
        SearchScreenInternal(
            search = { },
            updateQuery = { },
            updateTabIndex = { },
            uiState = uiState,
            routeNavigator = MyRouteNavigator()
        )
    }
}



















