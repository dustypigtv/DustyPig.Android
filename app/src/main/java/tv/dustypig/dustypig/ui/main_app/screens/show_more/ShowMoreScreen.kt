package tv.dustypig.dustypig.ui.main_app.screens.show_more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.ui.composables.BasicMediaView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowMoreScreen(vm: ShowMoreViewModel) {

    val uiState by vm.uiState.collectAsState()
    val mediaItems: LazyPagingItems<BasicMedia> = vm.itemData.collectAsLazyPagingItems()


    val listState = rememberLazyGridState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.title) },
                navigationIcon = {
                    IconButton(onClick = { vm.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    ) { innerPadding ->

        if (mediaItems.itemCount == 0) {
            Box(modifier = Modifier.fillMaxSize()){

            }
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                contentPadding = PaddingValues(12.dp),
                state = listState
            ) {

                items(
                    mediaItems.itemCount
                ) { index ->
                    BasicMediaView(
                        mediaItems[index]!!,
                        vm,
                        Modifier.fillMaxWidth()
                    )
                }

            }
        }
    }
}
