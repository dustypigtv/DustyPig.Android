package tv.dustypig.dustypig.ui.main_app.screens.series_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.API
import tv.dustypig.dustypig.api.Genres
import tv.dustypig.dustypig.api.asString
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.download_manager.DownloadManager
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.main_app.DetailsScreenBaseViewModel
import tv.dustypig.dustypig.ui.main_app.ScreenLoadingInfo
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title.ManageParentalControlsForTitleNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import javax.inject.Inject

@HiltViewModel
class SeriesDetailsViewModel  @Inject constructor(
    routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle
): DetailsScreenBaseViewModel(routeNavigator) {

    private val _uiState = MutableStateFlow(SeriesDetailsUIState())
    val uiState: StateFlow<SeriesDetailsUIState> = _uiState.asStateFlow()

    private val _titleInfoUIState = getTitleInfoUIStateForUpdate()

    override val mediaId: Int = savedStateHandle.getOrThrow(SeriesDetailsNav.KEY_ID)
    private lateinit var _detailedSeries: DetailedSeries
    private var _allEpisodes: List<DetailedEpisode> = listOf()

    init {

        _titleInfoUIState.update {
            it.copy(title = ScreenLoadingInfo.title)
        }

        _uiState.update {
            it.copy(
                posterUrl = ScreenLoadingInfo.posterUrl,
                backdropUrl = ScreenLoadingInfo.backdropUrl
            )
        }


        viewModelScope.launch {
            try {
                _detailedSeries = API.Series.seriesDetails(mediaId)
                _allEpisodes = _detailedSeries.episodes ?: listOf()
                if(_allEpisodes.isEmpty()) {
                    throw Exception("No episodes found.")
                }

                val allSeasons = ArrayList<UShort>()
                for(ep in _allEpisodes) {
                    if(!allSeasons.contains(ep.seasonNumber))
                        allSeasons.add(ep.seasonNumber)
                }

                val upNext: DetailedEpisode = _allEpisodes.firstOrNull { it.upNext } ?: _allEpisodes.first()
                val selEps = _allEpisodes.filter {
                    it.seasonNumber == upNext.seasonNumber
                }

                val unPlayed = upNext.id == _allEpisodes.first().id && (upNext.played == null || upNext.played < 1);
                val fullyPlayed = upNext.id == _allEpisodes.last().id && (upNext.played ?: 0.0) >= (upNext.creditStartTime ?: (upNext.length - 30.0))

                _uiState.update {
                    it.copy(
                        loading = false,
                        posterUrl = _detailedSeries.artworkUrl,
                        backdropUrl = _detailedSeries.backdropUrl ?: "",
                        seasons = allSeasons.toList(),
                        selectedSeason = upNext.seasonNumber,
                        episodes = selEps,
                        creditsData = CreditsData(
                            genres = Genres(_detailedSeries.genres).toList(),
                            cast = _detailedSeries.cast ?: listOf(),
                            directors = _detailedSeries.directors ?: listOf(),
                            producers = _detailedSeries.producers ?: listOf(),
                            writers = _detailedSeries.writers ?: listOf(),
                            owner = _detailedSeries.owner ?: ""
                        )
                    )
                }

                _titleInfoUIState.update {
                    it.copy(
                        playClick = { playUpNext() },
                        toggleWatchList = { toggleWatchList() },
                        download = { showDownloadDialog() },
                        addToPlaylist = { addToPlaylist() },
                        markWatched = { showMarkWatched() },
                        requestAccess = { requestAccess() },
                        manageClick = { manageParentalControls() },
                        inWatchList = _detailedSeries.inWatchlist,
                        title = _detailedSeries.title,
                        canManage = _detailedSeries.canManage,
                        canPlay = _detailedSeries.canPlay,
                        rated = _detailedSeries.rated.asString(),
                        overview = (if(unPlayed) _detailedSeries.description else upNext.description) ?: "",
                        partiallyPlayed = !(unPlayed || fullyPlayed),
                        seasonEpisode = if(unPlayed) "" else "S${upNext.seasonNumber}E${upNext.episodeNumber}",
                        episodeTitle = if(unPlayed) "" else upNext.title,
                        accessRequestStatus = _detailedSeries.accessRequestStatus,
                        accessRequestBusy = false,
                        mediaId = mediaId
                    )
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error",
                        criticalError = true
                    )
                }
            }
        }
    }

    fun setSeason(season: UShort) {

        val selEps = _allEpisodes.filter {
            it.seasonNumber == season
        }

        _uiState.update {
            it.copy(
                selectedSeason = season,
                episodes = selEps
            )
        }
    }

    fun hideError() {
        if(_uiState.value.criticalError) {
            popBackStack()
        }
        else {
            _uiState.update {
                it.copy(showError = false)
            }
        }
    }


    private fun playUpNext() {
        val upNext: DetailedEpisode = _allEpisodes.firstOrNull { it.upNext } ?: _allEpisodes.first()
        navigateToRoute(PlayerNav.getRouteForId(upNext.id))
    }

    fun playEpisode(id: Int) {
        navigateToRoute(PlayerNav.getRouteForId(id))
    }

    private fun showDownloadDialog() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showDownloadDialog = true,
                    currentDownloadCount = DownloadManager.getJobCount(mediaId)
                )
            }
        }
    }

    fun hideDownloadDialog(newCount: Int) {
        _uiState.update {
            it.copy(showDownloadDialog = false)
        }
        viewModelScope.launch {
            if (newCount == 0)
                DownloadManager.delete(_detailedSeries.id)
            else
                DownloadManager.addOrUpdateSeries(_detailedSeries, newCount)
        }
    }


    private fun requestAccess() {
        _titleInfoUIState.update {
            it.copy(accessRequestBusy = true)
        }

        viewModelScope.launch {
            try{
                API.Media.requestAccessOverride(mediaId)
                _titleInfoUIState.update {
                    it.copy(
                        accessRequestBusy = false,
                        accessRequestStatus = OverrideRequestStatus.Requested
                    )
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }

                _titleInfoUIState.update {
                    it.copy(
                        accessRequestBusy = false
                    )
                }
            }
        }
    }

    private fun toggleWatchList() {
        _titleInfoUIState.update {
            it.copy(watchListBusy = true)
        }

        viewModelScope.launch {

            try {

                if(_titleInfoUIState.value.inWatchList) {
                    API.Media.deleteFromWatchlist(mediaId)
                } else {
                    API.Media.addToWatchlist(mediaId)
                }

                _titleInfoUIState.update {
                    it.copy(
                        watchListBusy = false,
                        inWatchList = _titleInfoUIState.value.inWatchList.not()
                    )
                }

                HomeViewModel.triggerUpdate()
            } catch (ex: Exception) {
                _titleInfoUIState.update {
                    it.copy(
                        watchListBusy = false
                    )
                }
                _uiState.update{
                    it.copy(
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }

    private fun showMarkWatched() {
        _uiState.update {
            it.copy(showMarkWatchedDialog = true)
        }
    }

    fun hideMarkWatched(removeFromContinueWatching: Boolean) {
        _uiState.update {
            it.copy(showMarkWatchedDialog = false)
        }

        _titleInfoUIState.update {
            it.copy(markWatchedBusy = true)
        }

        viewModelScope.launch {
            try{
                if(removeFromContinueWatching) {
                    API.Series.removeFromContinueWatching(mediaId)
                } else {
                    API.Series.markSeriesWatched(mediaId)
                }
                _titleInfoUIState.update {
                    it.copy(
                        markWatchedBusy = false,
                        partiallyPlayed = false
                    )
                }
                HomeViewModel.triggerUpdate()
            } catch (ex: Exception) {
                _titleInfoUIState.update {
                    it.copy(markWatchedBusy = false)
                }
                _uiState.update {
                    it.copy(
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }


    private fun addToPlaylist() {
        navigateToRoute(AddToPlaylistNav.getRouteForId(mediaId, true))
    }

    private fun manageParentalControls() {
        navigateToRoute(ManageParentalControlsForTitleNav.getRouteForId(mediaId))
    }

    fun navToEpisodeInfo(id: Int) {
        ScreenLoadingInfo.setInfo(_detailedSeries.title, _detailedSeries.artworkUrl, _detailedSeries.backdropUrl ?: "")
        navigateToRoute(EpisodeDetailsNav.getRoute(id, _detailedSeries.canPlay, true))
    }
}