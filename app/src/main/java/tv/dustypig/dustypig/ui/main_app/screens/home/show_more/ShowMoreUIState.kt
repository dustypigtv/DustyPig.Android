package tv.dustypig.dustypig.ui.main_app.screens.home.show_more


data class ShowMoreUIState(
    val title: String = "",
    val initialLoad: Boolean = true,
    val loadingMore: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String = ""
)