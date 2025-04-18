package tv.dustypig.dustypig.ui.main_app.screens.movie_details

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.api.models.TitleRequestPermissions
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.ui.composables.CreditsData

@OptIn(UnstableApi::class)
data class MovieDetailsUIState(
    //Data
    val loading: Boolean = true,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val creditsData: CreditsData = CreditsData(),
    val castManager: CastManager? = null,
    val title: String = "",
    val year: String = "",
    val rated: String = "",
    val length: String = "",
    val overview: String = "",
    val canManage: Boolean = false,
    val canPlay: Boolean = false,
    val partiallyPlayed: Boolean = false,
    val markWatchedBusy: Boolean = false,
    val inWatchList: Boolean = false,
    val watchListBusy: Boolean = false,
    val titleRequestPermissions: TitleRequestPermissions = TitleRequestPermissions.Enabled,
    val accessRequestStatus: OverrideRequestStatus = OverrideRequestStatus.NotRequested,
    val accessRequestBusy: Boolean = false,
    val downloadStatus: DownloadStatus = DownloadStatus.None,
    val downloadPercent: Float = 0f,
    val downloadingForPlaylist: Boolean = false,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onHideError: () -> Unit = { },
    val onAddDownload: () -> Unit = { },
    val onRemoveDownload: () -> Unit = { },
    val onManagePermissions: () -> Unit = { },
    val onPlay: () -> Unit = { },
    val onToggleWatchlist: () -> Unit = { },
    val onAddToPlaylist: () -> Unit = { },
    val onMarkWatched: () -> Unit = { },
    val onRequestAccess: () -> Unit = { }
)

























