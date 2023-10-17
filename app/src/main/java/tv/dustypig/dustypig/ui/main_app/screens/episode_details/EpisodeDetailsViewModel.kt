package tv.dustypig.dustypig.ui.main_app.screens.episode_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.repositories.EpisodesRepository
import tv.dustypig.dustypig.api.toTimeString
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerViewModel
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav
import javax.inject.Inject

@HiltViewModel
class EpisodeDetailsViewModel  @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val episodesRepository: EpisodesRepository,
    private val downloadManager: DownloadManager,
    savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(EpisodeDetailsUIState())
    val uiState: StateFlow<EpisodeDetailsUIState> = _uiState.asStateFlow()

    private val _mediaId: Int = savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_ID)
    private val _canPlay: Boolean = savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_CAN_PLAY)
    private val _fromSeriesDetails: Boolean = savedStateHandle.getOrThrow(EpisodeDetailsNav.KEY_FROM_SERIES_DETAILS)

    private lateinit var _detailedEpisode: DetailedEpisode

    init {

        _uiState.update {
            it.copy(
                artworkUrl = ScreenLoadingInfo.backdropUrl,
                episodeTitle = ScreenLoadingInfo.title
            )
        }

        viewModelScope.launch {
            downloadManager.downloads.collectLatest {listOfJobs ->
                val job = listOfJobs.firstOrNull{
                    it.mediaId == _mediaId && it.mediaType == MediaTypes.Episode
                }
                if(job == null) {
                    _uiState.update {
                        it.copy(downloadStatus = DownloadStatus.None)
                    }
                } else {
                    _uiState.update {
                        it.copy(downloadStatus = job.status)
                    }
                }
            }
        }


        viewModelScope.launch {
            try {
                _detailedEpisode = episodesRepository.details(_mediaId)
                _uiState.update {
                    it.copy(
                        mediaId = _mediaId,
                        loading = false,
                        canPlay = _canPlay,
                        episodeTitle = _detailedEpisode.fullDisplayTitle(),
                        overview = _detailedEpisode.description ?: "No description",
                        seriesTitle = _detailedEpisode.seriesTitle!!,
                        showGoToSeries = !_fromSeriesDetails,
                        length = _detailedEpisode.length.toTimeString()
                    )
                }

                // Prevent flicker by only updating if needed
                if(_detailedEpisode.artworkUrl != _uiState.value.artworkUrl) {
                    _uiState.update {
                        it.copy(artworkUrl = _detailedEpisode.artworkUrl)
                    }
                }
            } catch(ex: Exception) {
                ex.logToCrashlytics()
                _uiState.update {
                    it.copy(
                        showErrorDialog = true,
                        criticalError = true,
                        errorMessage = ex.message
                    )
                }
            }
        }
    }


    fun hideError() {
        if(_uiState.value.criticalError) {
            popBackStack()
        }
        else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }


    fun addDownload() {
        viewModelScope.launch {
            downloadManager.addEpisode(_detailedEpisode)
        }
    }

    fun removeDownload() {
        viewModelScope.launch {
            downloadManager.delete(_mediaId, MediaTypes.Episode)
        }
    }

    fun play() {
        PlayerViewModel.mediaType = MediaTypes.Episode
        PlayerViewModel.detailedEpisode = _detailedEpisode
        navigateToRoute(PlayerNav.route)
    }

    fun addToPlaylist() {
        navigateToRoute(AddToPlaylistNav.getRouteForId(_mediaId, false))
    }

    fun goToSeries() {
        ScreenLoadingInfo.setInfo(_detailedEpisode.seriesTitle ?: "", "", "")
        navigateToRoute(SeriesDetailsNav.getRouteForId(_detailedEpisode.seriesId))
    }
}