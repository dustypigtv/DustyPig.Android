package tv.dustypig.dustypig.ui.main_app.screens.series_details

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.ThePig
import tv.dustypig.dustypig.api.Genres
import tv.dustypig.dustypig.api.asString
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.main_app.DetailsScreenBaseViewModel
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title.ManageParentalControlsForTitleNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import javax.inject.Inject

@HiltViewModel
class SeriesDetailsViewModel  @Inject constructor(
    private val routeNavigator: RouteNavigator
): DetailsScreenBaseViewModel(routeNavigator) {

    private val _uiState = MutableStateFlow(SeriesDetailsUIState())
    val uiState: StateFlow<SeriesDetailsUIState> = _uiState.asStateFlow()

    private val _titleInfoUIState = getTitleInfoUIStateForUpdate()

    private val _id: Int = ThePig.selectedBasicMedia.id
    private var _allEpisodes: List<DetailedEpisode> = listOf()

    init {
        _uiState.update {
            it.copy(
                loading = true,
                posterUrl = ThePig.selectedBasicMedia.artworkUrl
            )
        }

        _titleInfoUIState.update {
            it.copy(
                title = ThePig.selectedBasicMedia.title
            )
        }

        viewModelScope.launch {
            try {

                val data = ThePig.Api.Series.seriesDetails(_id)
                _allEpisodes = data.episodes ?: listOf()
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


                _uiState.update {
                    it.copy(
                        loading = false,
                        posterUrl = data.artworkUrl,
                        backdropUrl = data.backdropUrl ?: "",
                        seasons = allSeasons.toList(),
                        selectedSeason = upNext.seasonNumber,
                        episodes = selEps,
                        creditsData = CreditsData(
                            genres = Genres(data.genres).toList(),
                            cast = data.cast ?: listOf(),
                            directors = data.directors ?: listOf(),
                            producers = data.producers ?: listOf(),
                            writers = data.writers ?: listOf(),
                            owner = data.owner ?: ""
                        )
                    )
                }

                _titleInfoUIState.update {
                    it.copy(
                        playClick = { playUpNext() },
                        toggleWatchList = { toggleWatchList() },
                        download = { toggleDownload() },
                        addToPlaylist = { addToPlaylist() },
                        markWatched = { markWatched() },
                        requestAccess = { requestAccess() },
                        manageClick = { manageParentalControls() },
                        inWatchList = data.inWatchlist,
                        title = data.title,
                        canManage = data.canManage,
                        canPlay = data.canPlay,
                        rated = data.rated.asString(),
                        overview = data.description ?: "",
                        accessRequestStatus = data.accessRequestStatus,
                        accessRequestBusy = false,
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

    fun hideError(critical: Boolean) {
        if(critical) {
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

    private fun toggleDownload() {
//        if(DownloadManager.has(_id)) {
//            _uiState.update {
//                it.copy(showRemoveDownload = true)
//            }
//        } else {
//            DownloadManager.add(id = _id, mediaType = MediaTypes.Movie, count = 1)
//            _uiState.update {
//                it.copy(downloadStatus = DownloadStatus.Queued)
//            }
//        }
    }

    fun hideDownload(confirmed: Boolean) {
//        if(confirmed) {
//            DownloadManager.delete(id = _id)
//            _uiState.update {
//                it.copy(
//                    showRemoveDownload = false,
//                    downloadStatus = DownloadStatus.NotDownloaded
//                )
//            }
//        } else {
//            _uiState.update {
//                it.copy(showRemoveDownload = false)
//            }
//        }
    }

    private fun requestAccess() {
        _titleInfoUIState.update {
            it.copy(accessRequestBusy = true)
        }

        viewModelScope.launch {
            try{
                ThePig.Api.Media.requestAccessOverride(_id)
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
                    ThePig.Api.Media.deleteFromWatchlist(_id)
                } else {
                    ThePig.Api.Media.addToWatchlist(_id)
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

    private fun markWatched() {

        _titleInfoUIState.update {
            it.copy(markWatchedBusy = true)
        }

        viewModelScope.launch {
            try{
                ThePig.Api.Media.updatePlaybackProgress(id = _id, seconds = -1.0)
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
        navigateToRoute(AddToPlaylistNav.getRouteForId(_id, true))
    }

    private fun manageParentalControls() {
        navigateToRoute(ManageParentalControlsForTitleNav.getRouteForId(_id))
    }

    fun navToEpisodeInfo(id: Int) {
        ThePig.selectedDetailedEpisode = _allEpisodes.first { ep -> ep.id == id }
        navigateToRoute(EpisodeDetailsNav.getRouteForId(id))
    }
}