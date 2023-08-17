package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import tv.dustypig.dustypig.ui.download_manager.DownloadStatus

data class MovieDetailsUIState(
    val loading: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String = "",
    //val detailedMovie: DetailedMovie? = null,
    val downloadStatus: DownloadStatus = DownloadStatus.NotDownloaded,

    val inWatchList: Boolean = false,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val title: String = "",
    val year: String = "",
    val canManage: Boolean = false,
    val canPlay: Boolean = false,
    val rated: String = "",
    val length: String = "",
    val partiallyPlayed: Boolean = false,
    val description: String = "",
    val genres: List<String> = listOf(),
    val cast: List<String> = listOf(),
    val directors: List<String> = listOf(),
    val producers: List<String> = listOf(),
    val writers: List<String> = listOf(),
    val owner: String = ""
)