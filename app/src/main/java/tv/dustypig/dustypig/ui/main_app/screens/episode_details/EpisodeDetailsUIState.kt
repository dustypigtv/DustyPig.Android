package tv.dustypig.dustypig.ui.main_app.screens.episode_details

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus

@OptIn(UnstableApi::class)
data class EpisodeDetailsUIState (

    //Data
    val mediaId: Int = 0,
    val loading: Boolean = true,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val episodeTitle: String = "",
    val overview: String = "",
    val artworkUrl: String = "",
    val seriesTitle: String = "",
    val canPlay: Boolean = false,
    val showGoToSeries: Boolean = false,
    val length: String = "",
    val downloadStatus: DownloadStatus = DownloadStatus.None,
    val downloadPercent: Float = 0f,
    val downloadingForPlaylistOrSeries: Boolean = false,
    val castManager: CastManager? = null,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onHideError: () -> Unit = { },
    val onAddDownload: () -> Unit = { },
    val onRemoveDownload: () -> Unit = { },
    val onPlay: () -> Unit = { },
    val onAddToPlaylist: () -> Unit = { },
    val onGoToSeries: () -> Unit = { }
)