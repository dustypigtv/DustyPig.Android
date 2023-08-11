package tv.dustypig.dustypig.ui.main_app.screens.home

import tv.dustypig.dustypig.api.models.HomeScreenList


data class HomeScreenUIState(
    val isRefreshing: Boolean = true,
    val sections: List<HomeScreenList> = listOf(),
    val showError: Boolean = false,
    val errorMessage: String = "",
)