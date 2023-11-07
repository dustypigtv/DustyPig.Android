package tv.dustypig.dustypig.ui.main_app.screens.downloads

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.UIDownload
import tv.dustypig.dustypig.global_managers.download_manager.UIJob
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val downloadManager: DownloadManager
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        DownloadsUIState(
            onHideError = ::hideError,
            onDeleteAll = ::deleteAll,
            onDeleteDownload = ::deleteDownload,
            onModifyDownload = ::modifyDownload,
            onPlayItem = ::playItem,
            onPlayNext = ::playNext,
            onToggleExpansion = ::toggleExpansion
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _expandedMediaIds: ArrayList<Int> = arrayListOf()

    init {
        _uiState.update {
            it.copy(
                expandedMediaIds = _expandedMediaIds.toMutableStateList()
            )
        }
        viewModelScope.launch {
            downloadManager.downloads.collectLatest { jobLst ->
                _uiState.update {
                    it.copy(jobs = jobLst)
                }
            }
        }
    }


    private fun setError(ex: Exception) {
        _uiState.update {
            it.copy(
                showErrorDialog = true,
                errorMessage = ex.localizedMessage
            )
        }
        ex.printStackTrace()
    }

    fun hideError() {
        _uiState.update {
            it.copy(showErrorDialog = false)
        }
    }

    private fun toggleExpansion(id: Int) {
        if(_expandedMediaIds.contains(id))
            _expandedMediaIds.remove(id)
        else
            _expandedMediaIds.add(id)
        _uiState.update {
            it.copy(
                expandedMediaIds = _expandedMediaIds.toMutableStateList()
            )
        }
    }

    private fun playNext(job: UIJob) {
        TODO()
    }

    private fun playItem(job: UIJob, download: UIDownload) {
        TODO()
    }


    private fun deleteDownload(job: UIJob) {
        viewModelScope.launch {
            try {
                downloadManager.delete(job.mediaId, job.mediaType)
            } catch (ex: Exception) {
                setError(ex)
            }
        }
    }


    private fun modifyDownload(job: UIJob, newCount: Int) {
        viewModelScope.launch {
            try {
                when (job.mediaType) {
                    MediaTypes.Series -> downloadManager.updateSeries(job.mediaId, newCount)
                    MediaTypes.Playlist -> downloadManager.updatePlaylist(job.mediaId, newCount)
                    else -> {}
                }
            } catch (ex: Exception) {
                setError(ex)
            }
        }
    }

    private fun deleteAll() {
        viewModelScope.launch {
            try{
                downloadManager.deleteAll()
            } catch (ex: Exception) {
                setError(ex)
            }
        }
    }

}
