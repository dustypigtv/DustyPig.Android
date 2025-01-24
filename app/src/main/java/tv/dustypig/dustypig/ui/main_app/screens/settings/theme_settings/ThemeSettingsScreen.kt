package tv.dustypig.dustypig.ui.main_app.screens.settings.theme_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.settings_manager.Themes
import tv.dustypig.dustypig.ui.composables.CommonTopAppBar
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.composables.TintedIcon


@Composable
fun SettingsThemeScreen(vm: ThemeSettingsViewModel) {
    val uiState by vm.uiState.collectAsState()
    SettingsThemeScreenInternal(uiState = uiState)
}

@Composable
private fun SettingsThemeScreenInternal(uiState: ThemeSettingsUIState) {

    val themes = mapOf(
        Pair(stringResource(R.string.maggie_s), Themes.Maggies),
        Pair(stringResource(R.string.dusty_pig_classic), Themes.DustyPig),
        Pair(stringResource(R.string.lb), Themes.LB),
        Pair(stringResource(R.string.red_hot), Themes.Red),
        Pair(stringResource(R.string.neon_green), Themes.HuluGreen),
        Pair(stringResource(R.string.cool_blue), Themes.DisneyBlue),
        Pair(stringResource(R.string.burnt_orange), Themes.BurntOrange),
        Pair(stringResource(R.string.android_dark), Themes.AndroidDark)
    )

    Scaffold(
        topBar = {
            CommonTopAppBar(
                onClick = uiState.onPopBackStack,
                text = stringResource(R.string.select_theme)
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(12.dp))

            themes.forEach { theme ->
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .clickable { uiState.onSetTheme(theme.value) }
                        .background(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clip(shape = RoundedCornerShape(4.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {

//                    RadioButton(
//                        selected = uiState.currentTheme == theme.value,
//                        onClick = { },
//                    )

                    //Making the whole row clickable and using RB icons worked out a lot better than a real RB
                    val icon = if (uiState.currentTheme == theme.value)
                        Icons.Filled.RadioButtonChecked
                    else
                        Icons.Filled.RadioButtonUnchecked

                    TintedIcon(
                        imageVector = icon,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 12.dp,
                            end = 0.dp,
                            bottom = 12.dp
                        )
                    )

                    Text(
                        text = theme.key,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }

}

@Preview
@Composable
private fun SettingsThemeScreenPreview() {
    val uiState = ThemeSettingsUIState(
        currentTheme = Themes.Maggies
    )
    PreviewBase {
        SettingsThemeScreenInternal(uiState = uiState)
    }
}