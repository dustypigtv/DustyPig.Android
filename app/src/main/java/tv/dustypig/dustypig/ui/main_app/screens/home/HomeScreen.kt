package tv.dustypig.dustypig.ui.main_app.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideLazyListPreloader
import tv.dustypig.dustypig.ui.composables.BasicMediaView


@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(vm: HomeViewModel) {

    val uiState by vm.uiState.collectAsState()
    val ptrState = rememberPullRefreshState(uiState.isRefreshing, { vm.onRefresh() })
    val showLoading = remember { derivedStateOf { uiState.isRefreshing && uiState.sections.isEmpty() } }
    val showEmpty = remember { derivedStateOf { uiState.sections.isEmpty() && !uiState.isRefreshing }}
    val showLoadingOrEmpty = remember { derivedStateOf { showLoading.value || showEmpty.value }}

    Box(modifier = Modifier.fillMaxSize()) {

        if (showLoadingOrEmpty.value) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(ptrState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(showEmpty.value) {
                    Text(text = "No Media Available")
                } else {
                    Text(text = "Loading")
                }
            }
        } else {
            val configuration = LocalConfiguration.current
            val visibleGuess = remember { derivedStateOf { (configuration.screenWidthDp.dp / 100.dp).toInt() }}
            val preload = remember { derivedStateOf { visibleGuess.value * 2 }}
            val lazyColumnState = rememberLazyListState()
            val lazyRowStates = uiState.sections.associate { it.listId to rememberLazyListState() }
            for (section in uiState.sections) {
                GlideLazyListPreloader(
                    state = lazyRowStates[section.listId]!!,
                    data = section.items,
                    size = Size(100F, 150F),
                    numberOfItemsToPreload = preload.value,
                    fixedVisibleItemCount = visibleGuess.value
                ) { item, requestBuilder ->
                    requestBuilder.load(item.artworkUrl)
                }
            }

            LazyColumn(
                state = lazyColumnState,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(ptrState)
            ) {
                items(uiState.sections, key = { section -> section.listId }) { section ->
                    Column (
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = section.title,
                                fontWeight = FontWeight.Bold
                                )
                            if(section.items.size >= 25) {
                                TextButton(onClick = { vm.onShowMoreClicked(section) }) {
                                    Text(
                                        text = "More",
                                        style = TextStyle(textDecoration = TextDecoration.Underline)
                                    )
                                }
                            }
                        }
                        LazyRow(
                            state = lazyRowStates[section.listId]!!,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        ) {
                            items(section.items, key = { basicMedia -> basicMedia.id }) { basicMedia ->
                                BasicMediaView(
                                    basicMedia = basicMedia,
                                    vm
                                )
                            }
                        }

                    }
                }
            }
        }

        PullRefreshIndicator(
            uiState.isRefreshing,
            ptrState,
            Modifier.align(Alignment.TopCenter)
        )

    }
}