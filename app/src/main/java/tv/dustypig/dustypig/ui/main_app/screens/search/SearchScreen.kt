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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicTMDB
import tv.dustypig.dustypig.api.models.TMDB_MediaTypes
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.composables.BasicMediaView
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo
import tv.dustypig.dustypig.ui.main_app.screens.tmdb_details.TMDBDetailsNav

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(vm: SearchViewModel) {

    val uiState by vm.uiState.collectAsState()
    var query by remember { mutableStateOf("")}
    var ltquery by remember { mutableStateOf("")}
    val keyboardController = LocalSoftwareKeyboardController.current
    val availableState = rememberLazyGridState()
    val tmdbState = rememberLazyGridState()

    fun updateQuery(q: String) {
        query = q
        ltquery = q.lowercase().trim()
        vm.search(ltquery)
    }

    LaunchedEffect(ltquery) {
        availableState.scrollToItem(0)
        tmdbState.scrollToItem(0)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedTextField(
            value = query,
            onValueChange = ::updateQuery,
            placeholder = { Text(text = stringResource(R.string.search)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
            leadingIcon = {
                TintedIcon(
                    imageVector = Icons.Filled.Search
                )
            },
            trailingIcon = {
                if(query.isNotEmpty()) {
                    IconButton(onClick = { updateQuery("") }) {
                        TintedIcon(
                            imageVector = Icons.Filled.Cancel,
                        )
                    }
                }
            },
            shape = RoundedCornerShape(48.dp),

        )

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
                                            modifier = Modifier.size(36.dp).background(Color.Transparent),
                                            contentDescription = null
                                        )
                                        Text(text = stringResource(R.string.available))
                                    }
                                },
                                selected = uiState.tabIndex == 0,
                                onClick = {
                                    vm.updateTabIndex(0)
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
                                    vm.updateTabIndex(1)
                                    keyboardController?.hide()
                                },
                            )
                        }

                        when (uiState.tabIndex) {
                            0 -> AvailableLayout(vm = vm, uiState = uiState, availableState)
                            1 -> TMDBLayout(vm = vm, uiState = uiState, tmdbState)
                        }

                    } else {
                        AvailableLayout(vm = vm, uiState = uiState, availableState)
                    }
                }


            } else {
                if(!uiState.progressOnly)
                    Text(text = stringResource(R.string.no_results))
            }

            if(uiState.loading)
                CircularProgressIndicator(modifier = Modifier.offset(x = 0.dp, y = (-48).dp))
        }

    }
}



@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AvailableLayout(vm: SearchViewModel, uiState: SearchUIState, listState: LazyGridState) {

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
                routeNavigator = vm,
                clicked = { keyboardController?.hide() }
            )
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TMDBLayout(vm: SearchViewModel, uiState: SearchUIState, listState: LazyGridState) {

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
                routeNavigator = vm,
                clicked = { keyboardController?.hide() }
            )
        }

    }
}



@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TMDBMediaView(
    basicTMDB: BasicTMDB,
    routeNavigator: RouteNavigator,
    clicked: ((Int) -> Unit)? = null
) {
    fun onClicked() {

        if(clicked != null)
            clicked(basicTMDB.tmdbId)


        ScreenLoadingInfo.setInfo(title = basicTMDB.title, posterUrl = basicTMDB.artworkUrl ?: "", backdropUrl = basicTMDB.backdropUrl ?: "")

        when (basicTMDB.mediaType) {
            TMDB_MediaTypes.Movie -> {
                routeNavigator.navigateToRoute(route = TMDBDetailsNav.getRouteForId(basicTMDB.tmdbId, true))
            }
            TMDB_MediaTypes.Series -> {
                routeNavigator.navigateToRoute(route = TMDBDetailsNav.getRouteForId(basicTMDB.tmdbId, false))
            }
        }
    }

    val wdp = 100.dp
    val hdp = 150.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(hdp)
    ) {
        GlideImage(
            model = basicTMDB.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.Center)
                .size(wdp, hdp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onClicked() }
        ) {
            it
                .placeholder(R.drawable.placeholder_tall)
                .error(R.drawable.error_tall)
        }
    }
}
