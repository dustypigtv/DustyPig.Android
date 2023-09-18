package tv.dustypig.dustypig.ui.main_app.screens.episode_details

//import tv.dustypig.dustypig.download_manager.DownloadManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.repositories.EpisodesRepository
import tv.dustypig.dustypig.api.toTimeString
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import tv.dustypig.dustypig.ui.main_app.screens.series_details.SeriesDetailsNav
import javax.inject.Inject

@HiltViewModel
class EpisodeDetailsViewModel  @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val episodesRepository: EpisodesRepository,
    private val downloadManager: DownloadManager,
    savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(EpisodeDetailsUIState(downloadManager = downloadManager))
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

                //Prevent flicker
                if(_uiState.value.artworkUrl != _detailedEpisode.artworkUrl)
                    _uiState.update {
                        it.copy(artworkUrl = _detailedEpisode.artworkUrl)
                    }
            } catch(ex: Exception) {
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

    fun toggleDownload() {
        viewModelScope.launch {
            if (downloadManager.getJobCount(_mediaId) > 0) {
                _uiState.update {
                    it.copy(showRemoveDownloadDialog = true)
                }
            } else {
                downloadManager.addEpisode(_detailedEpisode)
            }
        }
    }

    fun hideDownload(confirmed: Boolean) {
        if(confirmed) {
            viewModelScope.launch {
                //DownloadManager.delete(id = _mediaId)
            }
            _uiState.update {
                it.copy(
                    showRemoveDownloadDialog = false
                )
            }
        } else {
            _uiState.update {
                it.copy(showRemoveDownloadDialog = false)
            }
        }
    }

    fun play() {
        navigateToRoute(PlayerNav.getRouteForId(_mediaId))
    }

    fun addToPlaylist() {
        navigateToRoute(AddToPlaylistNav.getRouteForId(_mediaId, false))
    }

    fun goToSeries() {
        ScreenLoadingInfo.setInfo(_detailedEpisode.seriesTitle ?: "", "", "")
        navigateToRoute(SeriesDetailsNav.getRouteForId(_detailedEpisode.seriesId))
    }
}