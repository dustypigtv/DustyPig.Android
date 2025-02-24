package tv.dustypig.dustypig.ui.main_app.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.BuildConfig
import tv.dustypig.dustypig.ui.composables.LazyColumnBottomAlign
import tv.dustypig.dustypig.ui.composables.TintedIcon


@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val uiState by vm.uiState.collectAsState()
    SettingsScreenInternal(uiState = uiState)
}


@Composable
private fun SettingsScreenInternal(
    uiState: SettingsUIState
) {

    val listState = rememberLazyListState()

    LazyColumnBottomAlign (
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        for(link in uiState.links) {
            item(key = link.resourceId) {
                LinkRow(
                    text = stringResource(link.resourceId),
                    onClick = { uiState.onNavToRoute(link.route) })
            }
        }


        item {
            Spacer(modifier = Modifier.height(24.dp))

            val version = "Version: " + BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE
            Text("Dusty Pig")
            Text(version)

            Spacer(modifier = Modifier.height(24.dp))
        }
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
            modifier = Modifier.padding(12.dp, 16.dp),
            color = MaterialTheme.colorScheme.primary
        )
        TintedIcon(
            imageVector = Icons.Filled.ChevronRight,
            modifier = Modifier.padding(12.dp, 16.dp)
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}
//
//@Preview
//@Composable
//private fun SettingsScreenPreview() {
//
//    val uiState = SettingsUIState(
//        isMainProfile = true
//    )
//
//    PreviewBase {
//        Scaffold {
//            Box(modifier = Modifier.padding(it)) {
//                SettingsScreenInternal(uiState = uiState)
//            }
//        }
//    }
//}






























