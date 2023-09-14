package tv.dustypig.dustypig.ui.main_app.screens.episode_details

data class EpisodeDetailsUIState(
    val mediaId: Int = 0,
    val loading: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val criticalError: Boolean = false,
    val showRemoveDownloadDialog: Boolean = false,
    val episodeTitle: String = "",
    val overview: String = "",
    val artworkUrl: String = "",
    val seriesTitle: String = "",
    val canPlay: Boolean = false,
    val showGoToSeries: Boolean = false,
    val length: String = "",
)