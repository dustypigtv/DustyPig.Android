package tv.dustypig.dustypig.ui.main_app.screens.series_details

import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.ui.composables.CreditsData

data class SeriesDetailsUIState(
    val loading: Boolean = true,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val creditsData: CreditsData = CreditsData(),
    val seasons: List<UShort> = listOf(),
    val upNextSeason: UShort = 0u,
    val selectedSeason: UShort = 0u,
    val episodes: List<DetailedEpisode> = listOf()
)
