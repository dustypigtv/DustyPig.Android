package tv.dustypig.dustypig.ui.main_app.screens.search

import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicTMDB

data class SearchUIState (
    val loading: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val hasResults: Boolean = false,
    val emptyQuery: Boolean = true,
    val progressOnly: Boolean = false,
    val allowTMDB: Boolean = false,
    val availableItems: List<BasicMedia> = listOf(),
    val tmdbItems: List<BasicTMDB> = listOf(),
    val tabIndex: Int = 0
)