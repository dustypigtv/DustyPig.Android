package tv.dustypig.dustypig.ui.main_app.screens.playlist_details

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import tv.dustypig.dustypig.api.models.PlaylistItem
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus

@OptIn(UnstableApi::class)
data class PlaylistDetailsUIState(

    //Data
    val loading: Boolean = true,
    val busy: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val currentDownloadCount: Int = 0,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val title: String = "",
    val canPlay: Boolean = false,
    val upNextTitle: String = "",
    val partiallyPlayed: Boolean = false,
    val playlistId: Int = 0,
    val items: List<PlaylistItem> = listOf(),
    val updateList: Boolean = false,
    val downloadStatus: DownloadStatus = DownloadStatus.None,
    val castManager: CastManager? = null,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onHideError: () -> Unit = { },
    val onListUpdated: () -> Unit = { },
    val onUpdateListOnServer: (from: Int, to: Int) -> Unit = { _, _ -> },
    val onPlayUpNext: () -> Unit = { },
    val onPlayItem: (id: Int) -> Unit = { },
    val onDeletePlaylist: () -> Unit = { },
    val onDeleteItem: (id: Int) -> Unit = { },
    val onNavToItem: (id: Int) -> Unit = { },
    val onRenamePlaylist: (newName: String) -> Unit = { },
    val onUpdateDownloads: (newCount: Int) -> Unit = { }
)






















