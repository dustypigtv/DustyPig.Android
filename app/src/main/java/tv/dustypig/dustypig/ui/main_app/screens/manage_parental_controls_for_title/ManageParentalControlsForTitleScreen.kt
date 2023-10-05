package tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.OverrideState
import tv.dustypig.dustypig.api.models.ProfileTitleOverrideInfo
import tv.dustypig.dustypig.api.models.TitlePermissionInfo
import tv.dustypig.dustypig.global_managers.settings_manager.Themes
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.ErrorDialog
import tv.dustypig.dustypig.ui.theme.DustyPigTheme

@Composable
fun ManageParentalControlsForTitleScreen(vm: ManageParentalControlsForTitleViewModel) {
    val uiState by vm.uiState.collectAsState()
    ManageParentalControlsForTitleScreenInternal(
        popBackStack = vm::popBackStack,
        togglePermission = vm::togglePermission,
        saveChanges = vm::saveChanges,
        hideError = vm::hideError,
        uiState = uiState
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ManageParentalControlsForTitleScreenInternal(
    popBackStack: () -> Unit,
    togglePermission: (Int) -> Unit,
    saveChanges: (Context) -> Unit,
    hideError: () -> Unit,
    uiState: ManageParentalControlsForTitleUIState
) {

    val listState = rememberLazyListState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CommonTopAppBar(onClick = popBackStack, text = stringResource(R.string.parental_controls))
        }
    ) { innerPadding ->

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = listState
        ){

            stickyHeader {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.profile),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.allowed),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(uiState.permissionInfo.profiles) {

                var checked by remember{
                    mutableStateOf(it.state == OverrideState.Allow)
                }

                fun toggle() {
                    togglePermission(it.profileId)
                    checked = checked.not()
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it.name,
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp, 0.dp)
                    )

                    Switch(
                        modifier = Modifier.padding(12.dp, 0.dp),
                        checked = checked,
                        enabled = !uiState.busy,
                        onCheckedChange = { toggle() })
                }

            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                   if(uiState.busy) {
                        CircularProgressIndicator()
                   } else {
                       Button(
                           enabled = uiState.pendingChanges,
                           onClick = { saveChanges(context) }
                       ) {
                           Text(text = stringResource(R.string.save))
                       }
                   }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

        }


    }


    if(uiState.showErrorDialog) {
        ErrorDialog(
            onDismissRequest = hideError,
            message = uiState.errorMessage
        )
    }

}


@Preview
@Composable
private fun ManageParentalControlsForTitleScreenPreview() {

    val uiState = ManageParentalControlsForTitleUIState(
        permissionInfo = TitlePermissionInfo(
            titleId = 0,
            profiles = listOf(
                ProfileTitleOverrideInfo(
                    profileId = 0,
                    state = OverrideState.Allow,
                    name = "Profile 1"
                ),
                ProfileTitleOverrideInfo(
                    profileId = 1,
                    state = OverrideState.Block,
                    name = "Profile 2"
                )
            )
        )
    )

    DustyPigTheme(currentTheme = Themes.Maggies) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ManageParentalControlsForTitleScreenInternal(
                popBackStack = { },
                togglePermission = { _ -> },
                saveChanges = { _ -> },
                hideError = { },
                uiState = uiState
            )
        }
    }
}