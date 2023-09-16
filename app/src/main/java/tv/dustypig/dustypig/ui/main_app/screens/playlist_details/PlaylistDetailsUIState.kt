package tv.dustypig.dustypig.ui.main_app.screens.playlist_details

import tv.dustypig.dustypig.api.models.PlaylistItem

data class PlaylistDetailsUIState (
    val loading: Boolean = true,
    val busy: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val showDownloadDialog: Boolean = false,
    val currentDownloadCount: Int = 0,
    val posterUrl: String = "",
    val title: String = "",
    val showRenameDialog: Boolean = false,
    val canPlay: Boolean = false,
    val upNextTitle: String = "",
    val partiallyPlayed: Boolean = false,
    val playlistId: Int = 0,
    val items: List<PlaylistItem> = listOf(),
    val updateList: Boolean = false,
    val showDeleteDialog: Boolean = false
)