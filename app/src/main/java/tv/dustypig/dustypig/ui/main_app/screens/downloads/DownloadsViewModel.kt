package tv.dustypig.dustypig.ui.main_app.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.UIJob
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    val downloadManager: DownloadManager
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(DownloadsUIState())
    val uiState = _uiState.asStateFlow()

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

    fun playNext(mediaId: Int, mediaType: MediaTypes) {

    }

    fun playItem(jobMediaId: Int, mediaType: MediaTypes, itemMediaId: Int) {

    }

    fun showDownloadDialog(mediaId: Int, mediaType: MediaTypes) {
        viewModelScope.launch {
            val job = downloadManager.downloads.first().firstOrNull{
                it.mediaId == mediaId && it.mediaType == mediaType
            }
            if(job != null){
                _uiState.update {
                    it.copy(
                        showDownloadDialog = true,
                        downloadDialogCount = job.count,
                        downloadDialogJobMediaId = job.mediaId,
                        downloadDialogJobMediaType = mediaType
                    )
                }
            }
        }
    }

    fun removeDownload(job: UIJob) {
        viewModelScope.launch {
            try {
                downloadManager.delete(job.mediaId, job.mediaType)
            } catch (ex: Exception) {
                setError(ex)
            }
        }
    }

    fun modifyDownload(newCount: Int) {
        _uiState.update {
            it.copy(showDownloadDialog = false)
        }
        viewModelScope.launch {
            try {
                when (_uiState.value.downloadDialogJobMediaType) {
                    MediaTypes.Series -> downloadManager.updateSeries(_uiState.value.downloadDialogJobMediaId, newCount)
                    MediaTypes.Playlist -> downloadManager.updatePlaylist(_uiState.value.downloadDialogJobMediaId, newCount)
                    else -> {}
                }
            } catch (ex: Exception) {
                setError(ex)
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            try{
                downloadManager.deleteAll()
            } catch (ex: Exception) {
                setError(ex)
            }
        }
    }

}
