package tv.dustypig.dustypig.ui.main_app.screens.downloads

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import tv.dustypig.dustypig.global_managers.download_manager.UIJob

data class DownloadsUIState(
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val jobs: List<UIJob> = listOf(),
    val expandedMediaIds: SnapshotStateList<Int> = mutableStateListOf()
)
