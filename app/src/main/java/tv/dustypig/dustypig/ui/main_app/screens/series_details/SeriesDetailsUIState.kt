package tv.dustypig.dustypig.ui.main_app.screens.series_details

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.api.models.TitleRequestPermissions
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.ui.composables.CreditsData

@OptIn(UnstableApi::class)
data class SeriesDetailsUIState(

    //Data
    val loading: Boolean = true,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val creditsData: CreditsData = CreditsData(),
    val seasons: List<UShort> = listOf(),
    val selectedSeason: UShort = 0u,
    val episodes: List<DetailedEpisode> = listOf(),
    val castManager: CastManager? = null,
    val title: String = "",
    val rated: String = "",
    val overview: String = "",
    val canManage: Boolean = false,
    val canPlay: Boolean = false,
    val partiallyPlayed: Boolean = false,
    val markWatchedBusy: Boolean = false,
    val inWatchList: Boolean = false,
    val watchListBusy: Boolean = false,
    val upNextEpisode: UShort? = null,
    val upNextSeason: UShort? = 0u,
    val episodeTitle: String = "",
    val titleRequestPermissions: TitleRequestPermissions = TitleRequestPermissions.Enabled,
    val accessRequestStatus: OverrideRequestStatus = OverrideRequestStatus.NotRequested,
    val accessRequestBusy: Boolean = false,
    val downloadStatus: DownloadStatus = DownloadStatus.None,
    val downloadPercent: Float = 0f,
    val currentDownloadCount: Int = 0,
    val subscribed: Boolean = false,
    val subscribeBusy: Boolean = false,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onHideError: () -> Unit = { },
    val onPlay: () -> Unit = { },
    val onPlayEpisode: (id: Int) -> Unit = { },
    val onNavToEpisodeInfo: (id: Int) -> Unit = { },
    val onSelectSeason: (seasonNumber: UShort) -> Unit = { },
    val onToggleWatchList: () -> Unit = { },
    val onUpdateDownload: (Int) -> Unit = { },
    val onAddToPlaylist: () -> Unit = { },
    val onMarkWatched: (removeFromContinueWatching: Boolean) -> Unit = { },
    val onRequestAccess: () -> Unit = { },
    val onManagePermissions: () -> Unit = { },
    val onToggleSubscribe: () -> Unit = { }
)



