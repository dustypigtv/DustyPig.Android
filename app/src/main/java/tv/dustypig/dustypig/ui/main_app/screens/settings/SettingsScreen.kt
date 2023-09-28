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
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.ui.composables.TintedIcon


@Composable
fun SettingsScreen(vm: SettingsViewModel) {

    val uiState by vm.uiState.collectAsState()
    val listState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(listState)
    ) {

        if(uiState.isMainProfile) {
            LinkRow(vm = vm, text = "Account Settings", onClick = vm::navToAccountSettings)
            LinkRow(vm = vm, text = "Friends", onClick = vm::navToFriendsSettings)
        }

        LinkRow(vm = vm, text = "Theme", onClick = vm::navToTheme)


    }
}

@Composable
private fun LinkRow(vm: SettingsViewModel, text: String, onClick: () -> Unit) {
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