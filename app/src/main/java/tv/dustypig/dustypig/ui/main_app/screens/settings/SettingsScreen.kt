package tv.dustypig.dustypig.ui.main_app.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.BuildConfig
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon
import tv.dustypig.dustypig.ui.main_app.screens.settings.account_settings.AccountSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.download_settings.DownloadSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.FriendsSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.notification_settings.NotificationSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.playback_settings.PlaybackSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.ProfilesSettingsNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.switch_profiles.SwitchProfilesNav
import tv.dustypig.dustypig.ui.main_app.screens.settings.theme_settings.ThemeSettingsNav


@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val uiState by vm.uiState.collectAsState()
    SettingsScreenInternal(uiState = uiState)
}


@Composable
private fun SettingsScreenInternal(
    uiState: SettingsUIState
) {

    val listState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(listState)
    ) {

        Spacer(modifier = Modifier.height(1.dp))


        LinkRow(
            text = stringResource(R.string.playback_settings),
            onClick = { uiState.onNavToRoute(PlaybackSettingsNav.route) })
        LinkRow(
            text = stringResource(R.string.notification_settings),
            onClick = { uiState.onNavToRoute(NotificationSettingsNav.route) })
        LinkRow(
            text = stringResource(R.string.download_settings),
            onClick = { uiState.onNavToRoute(DownloadSettingsNav.route) })
        LinkRow(
            text = stringResource(R.string.theme),
            onClick = { uiState.onNavToRoute(ThemeSettingsNav.route) })
        LinkRow(
            text = stringResource(R.string.switch_profiles),
            onClick = { uiState.onNavToRoute(SwitchProfilesNav.route) })

        LinkRow(
            text = stringResource(R.string.account_settings),
            onClick = { uiState.onNavToRoute(AccountSettingsNav.route) })

        if (uiState.isMainProfile) {
            LinkRow(
                text = stringResource(R.string.manage_profiles),
                onClick = { uiState.onNavToRoute(ProfilesSettingsNav.route) })
            LinkRow(
                text = stringResource(R.string.manage_friends),
                onClick = { uiState.onNavToRoute(FriendsSettingsNav.route) })
        } else {
            LinkRow(text = stringResource(R.string.my_profile), onClick = uiState.onNavToMyProfile)
        }

        Spacer(modifier = Modifier.height(24.dp))


        val version = "Version: " + BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE
        Text("Dusty Pig")
        Text(version)
    }
}

@Composable
private fun LinkRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(shape = RoundedCornerShape(4.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                shape = RoundedCornerShape(4.dp)
            )
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
        Scaffold {
            Box(modifier = Modifier.padding(it)) {
                SettingsScreenInternal(uiState = uiState)
            }
        }
    }
}






























