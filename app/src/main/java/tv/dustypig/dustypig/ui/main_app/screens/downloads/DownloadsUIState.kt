package tv.dustypig.dustypig.ui.main_app.screens.downloads

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import tv.dustypig.dustypig.global_managers.download_manager.UIDownload
import tv.dustypig.dustypig.global_managers.download_manager.UIJob

data class DownloadsUIState(

    //Data
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val jobs: List<UIJob> = listOf(),
    val expandedMediaIds: SnapshotStateList<Int> = mutableStateListOf(),

    //Events
    val onHideError: () -> Unit = { },
    val onPlayNext: (job: UIJob) -> Unit = { },
    val onPlayItem: (job: UIJob, download: UIDownload) -> Unit = { _, _ -> },
    val onDeleteDownload: (job: UIJob) -> Unit = { },
    val onDeleteAll: () -> Unit = { },
    val onToggleExpansion: (id: Int) -> Unit = { },
    val onModifyDownload: (job: UIJob, newCount: Int) -> Unit = { _, _ -> }
)
