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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicTMDB
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.TMDBMediaTypes
import tv.dustypig.dustypig.nav.MyRouteNavigator
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.composables.BasicMediaView
import tv.dustypig.dustypig.ui.composables.BasicPersonView
import tv.dustypig.dustypig.ui.composables.BasicTMDBView
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon


@Composable
fun SearchScreen(vm: SearchViewModel) {

    val uiState by vm.uiState.collectAsState()
    SearchScreenInternal(
        uiState = uiState,
        routeNavigator = vm
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenInternal(
    uiState: SearchUIState,
    routeNavigator: RouteNavigator
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val availableState = rememberLazyGridState()
    val tmdbState = rememberLazyGridState()
    var expanded by rememberSaveable { mutableStateOf(false) }


    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = uiState.query,
                    onQueryChange = uiState.onUpdateQuery,
                    onSearch = {
                        expanded = false
                        uiState.onSearch()
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    leadingIcon = {
                        TintedIcon(imageVector = Icons.Filled.Search)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (uiState.query.isEmpty()) {
                                    expanded = false
                                } else {
                                    uiState.onUpdateQuery("")
                                    uiState.onSearch()
                                }
                            }
                        ) {
                            TintedIcon(imageVector = Icons.Filled.Cancel)
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 0.dp),
            content = {
                uiState.history.forEach { history ->
                    Row(
                        modifier = Modifier
                            .clickable {
                                uiState.onUpdateQuery(history)
                                expanded = false
                                uiState.onSearch()
                            }
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TintedIcon(imageVector = Icons.Filled.History)
                        Text(text = history)
                    }
                }
            },
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            if (uiState.emptyQuery) {
                Text(
                    text = stringResource(R.string.enter_search),
                    color = MaterialTheme.colorScheme.primary
                )

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
                                    uiState.onUpdateTabIndex(0)
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
                                    uiState.onUpdateTabIndex(1)
                                    keyboardController?.hide()
                                },
                            )
                        }

                        when (uiState.tabIndex) {
                            0 -> AvailableLayout(
                                uiState = uiState,
                                listState = availableState,
                                routeNavigator = routeNavigator
                            )

                            1 -> TMDBLayout(
                                uiState = uiState,
                                listState = tmdbState,
                                routeNavigator = routeNavigator
                            )
                        }

                    } else {
                        AvailableLayout(
                            uiState = uiState,
                            listState = availableState,
                            routeNavigator = routeNavigator
                        )
                    }
                }


            } else {
                if (!uiState.progressOnly)
                    Text(text = stringResource(R.string.no_results))
            }

            if (uiState.busy)
                CircularProgressIndicator(modifier = Modifier.offset(x = 0.dp, y = (-48).dp))
        }

    }
}


@Composable
private fun AvailableLayout(
    uiState: SearchUIState,
    listState: LazyGridState,
    routeNavigator: RouteNavigator
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val cnt = uiState.availableItems.size + uiState.availablePeople.size

    if(cnt > 0) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            columns = GridCells.Adaptive(minSize = 116.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            contentPadding = PaddingValues(12.dp)
        ) {

            items(uiState.availableItems, key = { "${it.id}.m" }) {

                Box(
                    modifier = Modifier.padding(0.dp, 12.dp)
                ) {

                    BasicMediaView(
                        basicMedia = it,
                        routeNavigator = routeNavigator,
                        clicked = { keyboardController?.hide() }
                    )
                }
            }

            items(uiState.availablePeople, key = { "${it.tmdbId}.p" }) {
                BasicPersonView(
                    basicPerson = it,
                    routeNavigator = routeNavigator,
                    clicked = { keyboardController?.hide() }
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(R.string.no_results),
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TMDBLayout(
    uiState: SearchUIState,
    listState: LazyGridState,
    routeNavigator: RouteNavigator
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val cnt = uiState.tmdbItems.size + uiState.tmdbPeople.size

    if(cnt > 0) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            columns = GridCells.Adaptive(minSize = 116.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            contentPadding = PaddingValues(12.dp)
        ) {


            items(uiState.tmdbItems, key = { "${it.tmdbId}.${it.mediaType}" }) {

                Box(
                    modifier = Modifier.padding(0.dp, 12.dp)
                ) {
                    BasicTMDBView(
                        basicTMDB = it,
                        routeNavigator = routeNavigator,
                        clicked = { keyboardController?.hide() }
                    )
                }

            }

            items(uiState.tmdbPeople, key = { it.tmdbId }) {
                BasicPersonView(
                    basicPerson = it,
                    routeNavigator = routeNavigator,
                    clicked = { keyboardController?.hide() }
                )
            }

        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(R.string.no_results),
                color = MaterialTheme.colorScheme.primary)
        }
    }
}




@Preview
@Composable
private fun SearchScreenPreview() {

    val basicMediaList = arrayListOf<BasicMedia>()
    for (i in 1..10) {
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
    for (i in 1..10) {
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
            uiState = uiState,
            routeNavigator = MyRouteNavigator()
        )
    }
}



















