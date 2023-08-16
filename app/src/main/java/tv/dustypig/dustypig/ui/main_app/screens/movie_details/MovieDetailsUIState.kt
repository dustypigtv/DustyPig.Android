package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.ui.download_manager.DownloadStatus

data class MovieDetailsUIState(
    val loading: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val detailedMovie: DetailedMovie? = null,
    val downloadStatus: DownloadStatus = DownloadStatus.NotDownloaded,

    //These are in the DetailedMovie, but will be variable - so redefining them
    val inWatchList: Boolean = false,
    val artworkUrl: String = "",
    val isPoster: Boolean = true
)