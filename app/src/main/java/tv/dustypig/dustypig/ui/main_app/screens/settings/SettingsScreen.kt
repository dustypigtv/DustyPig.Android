package tv.dustypig.dustypig.ui.main_app.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon


@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val uiState by vm.uiState.collectAsState()

    SettingsScreenInternal(
        navToTheme = vm::navToTheme,
        navToAccountSettings = vm::navToAccountSettings,
        navToMyProfile = vm::navToMyProfile,
        navToFriendsSettings = vm::navToFriendsSettings,
        navToAllProfilesSettings = vm::navToAllProfilesSettings,
        uiState = uiState
    )
}


@Composable
private fun SettingsScreenInternal(
    navToTheme: () -> Unit,
    navToAccountSettings: () -> Unit,
    navToMyProfile: () -> Unit,
    navToFriendsSettings: () -> Unit,
    navToAllProfilesSettings: () -> Unit,
    uiState: SettingsUIState
) {

    val listState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(listState)
    ) {

        LinkRow(text = "Account Settings", onClick = navToAccountSettings)
        LinkRow(text = "My Profile", onClick = navToMyProfile)

        if(uiState.isMainProfile) {
            LinkRow(text = "Friends", onClick = navToFriendsSettings)
            LinkRow(text = "Profiles", onClick = navToAllProfilesSettings)
        }

        LinkRow(text = "Theme", onClick = navToTheme)
    }
}

@Composable
private fun LinkRow(text: String, onClick: () -> Unit) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(shape = RoundedCornerShape(4.dp))
            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = RoundedCornerShape(4.dp))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp, 16.dp)
        )
        TintedIcon(
            imageVector = Icons.Filled.ChevronRight,
            modifier = Modifier.padding(12.dp, 16.dp)
        )
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {

    val uiState = SettingsUIState(
        isMainProfile = true
    )

    PreviewBase {
        SettingsScreenInternal(
            navToTheme = { },
            navToAccountSettings = { },
            navToMyProfile = { },
            navToFriendsSettings = { },
            navToAllProfilesSettings = { },
            uiState = uiState
        )
    }
}






























