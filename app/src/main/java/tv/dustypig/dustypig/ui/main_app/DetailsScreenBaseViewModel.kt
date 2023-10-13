package tv.dustypig.dustypig.ui.main_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.composables.TitleInfoData

abstract class DetailsScreenBaseViewModel constructor(
    private val routeNavigator: RouteNavigator,
    internal val downloadManager: DownloadManager,
    private val mediaType: MediaTypes
): ViewModel(), RouteNavigator by routeNavigator {

    protected val baseTitleInfoUIState = MutableStateFlow(TitleInfoData())

    val titleInfoUIState: StateFlow<TitleInfoData> = baseTitleInfoUIState.asStateFlow()

    abstract val mediaId: Int

    init {
        viewModelScope.launch {
            downloadManager.downloads.collectLatest { jobLst ->
                val job = jobLst.firstOrNull {
                    it.mediaId == mediaId && it.mediaType == mediaType
                }
                if(job == null) {
                    baseTitleInfoUIState.update {
                        it.copy(
                            downloadStatus = DownloadStatus.None,
                            currentDownloadCount = 0
                        )
                    }
                } else {
                    baseTitleInfoUIState.update {
                        it.copy(
                            downloadStatus = job.status,
                            currentDownloadCount = job.count
                        )
                    }
                }
            }
        }

    }
}