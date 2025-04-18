package tv.dustypig.dustypig.ui.main_app.screens.downloads

import tv.dustypig.dustypig.global_managers.download_manager.UIDownload
import tv.dustypig.dustypig.global_managers.download_manager.UIJob

data class DownloadsUIState(

    //Data
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val jobs: List<UIJob> = listOf(),
    val downloadTutorialSeen: Boolean = true,

    //Events
    val onHideError: () -> Unit = { },
    val onPlayItem: (job: UIJob, download: UIDownload) -> Unit = { _, _ -> },
    val onDeleteDownload: (job: UIJob) -> Unit = { },
    val onDeleteAll: () -> Unit = { },
    val onModifyDownload: (job: UIJob, newCount: Int) -> Unit = { _, _ -> },
    val onDownloadTutorialSeen: () -> Unit = { }
)
