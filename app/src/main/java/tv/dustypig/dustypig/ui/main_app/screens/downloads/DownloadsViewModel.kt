package tv.dustypig.dustypig.ui.main_app.screens.downloads

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.global_managers.download_manager.MyDownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.UIDownload
import tv.dustypig.dustypig.global_managers.download_manager.UIJob
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class DownloadsViewModel
@Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val downloadManager: MyDownloadManager,
    private val settingsManager: SettingsManager
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        DownloadsUIState(
            onHideError = ::hideError,
            onDeleteAll = ::deleteAll,
            onDeleteDownload = ::deleteDownload,
            onModifyDownload = ::modifyDownload,
            onPlayItem = ::playItem,
            onDownloadTutorialSeen = ::downloadTutorialSeen
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    downloadTutorialSeen = settingsManager.getDownloadTutorialSeen()
                )
            }
        }

        viewModelScope.launch {
            downloadManager.currentDownloads.collectLatest { jobLst ->
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


    fun downloadTutorialSeen() {
        viewModelScope.launch {
            settingsManager.setDownloadTutorialSeen()
        }
        _uiState.update {
            it.copy(downloadTutorialSeen = true)
        }
    }


    private fun playItem(job: UIJob, download: UIDownload) {

        //In this case mediaId = JobId and upNextId = Download id

        navigateToRoute(
            PlayerNav.getRoute(
                mediaId = job.mediaId,
                upNextId = download.mediaId,
                sourceType = PlayerNav.MEDIA_TYPE_DOWNLOAD
            )
        )
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
        downloadManager.deleteAll()
    }

}
