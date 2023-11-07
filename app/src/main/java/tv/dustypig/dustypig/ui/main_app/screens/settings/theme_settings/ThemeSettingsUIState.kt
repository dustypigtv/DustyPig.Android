package tv.dustypig.dustypig.ui.main_app.screens.settings.theme_settings

import tv.dustypig.dustypig.global_managers.settings_manager.Themes

data class ThemeSettingsUIState(

    //Data
    val currentTheme: Themes = Themes.Maggies,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onSetTheme: (theme: Themes) -> Unit = { }
)
