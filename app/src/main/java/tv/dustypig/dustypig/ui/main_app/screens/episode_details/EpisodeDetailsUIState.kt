package tv.dustypig.dustypig.ui.main_app.screens.episode_details

import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager

data class EpisodeDetailsUIState(
    val mediaId: Int = 0,
    val loading: Boolean = true,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val showRemoveDownloadDialog: Boolean = false,
    val episodeTitle: String = "",
    val overview: String = "",
    val artworkUrl: String = "",
    val seriesTitle: String = "",
    val canPlay: Boolean = false,
    val showGoToSeries: Boolean = false,
    val length: String = "",
    val downloadManager: DownloadManager
)