package tv.dustypig.dustypig.ui.auth_flow.screens.select_profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute
import tv.dustypig.dustypig.ui.composables.Avatar
import tv.dustypig.dustypig.ui.composables.OkDialog
import tv.dustypig.dustypig.ui.composables.PinEntry
import tv.dustypig.dustypig.ui.theme.DarkGray

object SelectProfileScreenRoute : NavRoute<SelectProfileViewModel> {

    override val route = "selectProfile"

    @Composable
    override fun viewModel(): SelectProfileViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: SelectProfileViewModel) = SelectProfileScreen(viewModel)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectProfileScreen(vm: SelectProfileViewModel) {

    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyGridState()


    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Select Profile") },
                navigationIcon = {
                    IconButton(onClick = { vm.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = DarkGray)
            )
        }
    ) { contentPadding ->

        if (uiState.busy) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {

            LazyVerticalGrid(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 72.dp),
                verticalArrangement = Arrangement.spacedBy(36.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 24.dp),
                state = listState
            ) {
                items(uiState.profiles, key = { it.id }) {

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Avatar(basicProfile = it, onClick = { vm.onProfileSelected(it, context) })
                        Text(text = it.name)
                    }
                }
            }
        }
    }

    if(uiState.showError) {
        OkDialog(onDismissRequest = { vm.hideError() }, title = "Error", message = uiState.errorMessage)
    }

    if(uiState.showPinDialog) {

        val confirmEnabled = remember { derivedStateOf { uiState.pin.length == 4 }}

        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            onDismissRequest = { vm.cancelPinDialog() },
            title = { Text("Enter Pin") },
            text = {
                PinEntry(valueChanged = { vm.updatePin(it) }, autoFocus = true)
            },
            confirmButton = {
                TextButton(enabled = confirmEnabled.value,
                    onClick = { vm.onPinSubmitted(context) }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.cancelPinDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

