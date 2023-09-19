package tv.dustypig.dustypig.ui.main_app.screens.downloads

import tv.dustypig.dustypig.api.models.MediaTypes

data class DownloadsUIState(
    val showDownloadDialog: Boolean = false,
    val downloadDialogCount: Int = 0,
    val downloadDialogJobMediaId: Int = 0,
    val downloadDialogJobMediaType: MediaTypes = MediaTypes.Series,
    val showRemoveDownloadDialog: Boolean = false
)
