package tv.dustypig.dustypig.ui.main_app.screens.home

import tv.dustypig.dustypig.api.models.HomeScreenList


data class HomeUIState(

    //Data
    val isRefreshing: Boolean = true,
    val sections: List<HomeScreenList> = listOf(),
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val hasNetworkConnection: Boolean = false,

    //Events
    val onRefresh: () -> Unit = { },
    val onShowMoreClicked: (hsl: HomeScreenList) -> Unit = { }
)