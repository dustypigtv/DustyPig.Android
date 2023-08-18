package tv.dustypig.dustypig.ui.main_app.screens.series_details

import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.ui.download_manager.DownloadStatus

data class SeriesDetailsUIState(
    val loading: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val criticalError: Boolean = false,
    val downloadStatus: DownloadStatus = DownloadStatus.NotDownloaded,
    val inWatchList: Boolean = false,
    val watchListBusy: Boolean = false,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val title: String = "",
    val canManage: Boolean = false,
    val canPlay: Boolean = false,
    val rated: String = "",
    val partiallyPlayed: Boolean = false,
    val markWatchedBusy: Boolean = false,
    val description: String = "",
    val genres: List<String> = listOf(),
    val cast: List<String> = listOf(),
    val directors: List<String> = listOf(),
    val producers: List<String> = listOf(),
    val writers: List<String> = listOf(),
    val owner: String = "",
    val seasons: List<UShort> = listOf(),
    val selectedSeason: UShort = 0u,
    val episodes: List<DetailedEpisode> = listOf()
)
