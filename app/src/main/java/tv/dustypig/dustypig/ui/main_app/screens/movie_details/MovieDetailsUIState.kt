package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import tv.dustypig.dustypig.ui.composables.CreditsData

data class MovieDetailsUIState(
    val loading: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val criticalError: Boolean = false,
    val showRemoveDownload: Boolean = false,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val creditsData: CreditsData = CreditsData()
)
