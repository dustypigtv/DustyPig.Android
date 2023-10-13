package tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.ui.composables.Avatar
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon

@Composable
fun ProfilesSettingsScreen(vm: ProfilesSettingsViewModel){
    val uiState by vm.uiState.collectAsState()
    ProfilesSettingsScreenInternal(
        popBackStack = vm::popBackStack,
        hideError = vm::hideError,
        navToAddProfile = vm::navToAddProfile,
        navToProfile = vm::navToProfile,
        uiState = uiState
    )
}

@Composable
private fun ProfilesSettingsScreenInternal(
    popBackStack: () -> Unit,
    hideError: () -> Unit,
    navToAddProfile: () -> Unit,
    navToProfile: (Int) -> Unit,
    uiState: ProfilesSettingsUIState
) {

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = popBackStack, text = "Profiles")
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                item{
                    //Just some blank space between top and actual list
                }

                items(uiState.profiles, key = { it.clientUUID }) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp, 0.dp)
                            .clip(shape = RoundedCornerShape(4.dp))
                            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = RoundedCornerShape(4.dp))
                            .clickable { navToProfile(it.id) }
                    ) {
                        Avatar(
                            imageUrl = it.avatarUrl ?: "",
                            modifier = Modifier
                                .padding(12.dp)
                                .size(48.dp),
                            clickable = false
                        )

                        Text(
                            text = it.name,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        TintedIcon(
                            imageVector = Icons.Filled.ChevronRight,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                item {
                    Button(onClick = navToAddProfile) {
                        Text(text = "Add Profile")
                    }
                }

            }

            if(uiState.busy) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }


        if(uiState.showErrorDialog) {
            ErrorDialog(onDismissRequest = hideError, message = uiState.errorMessage)
        }
    }
}


@Preview
@Composable
private fun ProfilesSettingsScreenPreview() {
    val uiState = ProfilesSettingsUIState(
        busy = false,
        profiles = listOf(
            BasicProfile(
                id = 0,
                name = "Profile 1"
            ),
            BasicProfile(
                id = 1,
                name = "Profile 2"
            )
        )
    )

    PreviewBase {
        ProfilesSettingsScreenInternal(
            popBackStack = { },
            hideError = { },
            navToAddProfile = { },
            navToProfile = { _ -> },
            uiState = uiState
        )
    }
}