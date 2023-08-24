package tv.dustypig.dustypig.ui.main_app.screens.series_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.Genres
import tv.dustypig.dustypig.api.ThePig
import tv.dustypig.dustypig.api.asString
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class SeriesDetailsViewModel  @Inject constructor(
    private val routeNavigator: RouteNavigator
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(SeriesDetailsUIState())
    val uiState: StateFlow<SeriesDetailsUIState> = _uiState.asStateFlow()

    private val _id: Int = ThePig.selectedBasicMedia.id
    private var _allEpisodes: List<DetailedEpisode> = listOf()

    init {
        _uiState.update {
            it.copy(
                loading = true,
                title = ThePig.selectedBasicMedia.title,
                posterUrl = ThePig.selectedBasicMedia.artworkUrl
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
                var selEps = _allEpisodes.filter {
                    it.seasonNumber == upNext.seasonNumber
                }


                _uiState.update {
                    it.copy(
                        loading = false,
                        inWatchList = data.inWatchlist,
                        posterUrl = data.artworkUrl,
                        backdropUrl = data.backdropUrl ?: "",
                        title = data.title,
                        canManage = data.canManage,
                        canPlay = data.canPlay,
                        rated = data.rated.asString(),
                        description = data.description ?: "",
                        genres = Genres(data.genres).toList(),
                        cast = data.cast ?: listOf(),
                        directors = data.directors ?: listOf(),
                        producers = data.producers ?: listOf(),
                        writers = data.writers ?: listOf(),
                        owner = data.owner ?: "",
                        seasons = allSeasons.toList(),
                        selectedSeason = upNext.seasonNumber,
                        episodes = selEps
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


    fun playUpNext() {

    }

    fun playEpisode(id: Int) {

    }

    fun toggleDownload() {
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

    fun requestAccess() {

    }

    fun toggleWatchList() {
        _uiState.update {
            it.copy(watchListBusy = true)
        }

        viewModelScope.launch {

            try {

                if(_uiState.value.inWatchList) {
                    ThePig.Api.Media.deleteFromWatchlist(_id)
                } else {
                    ThePig.Api.Media.addToWatchlist(_id)
                }

                _uiState.update {
                    it.copy(
                        watchListBusy = false,
                        inWatchList = _uiState.value.inWatchList.not()
                    )
                }
            } catch (ex: Exception) {
                _uiState.update{
                    it.copy(
                        watchListBusy = false,
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }

    fun markWatched() {

        _uiState.update {
            it.copy(markWatchedBusy = true)
        }

        viewModelScope.launch {
            try{
                ThePig.Api.Media.updatePlaybackProgress(id = _id, seconds = -1.0)
                _uiState.update {
                    it.copy(
                        markWatchedBusy = false,
                        partiallyPlayed = false
                    )
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        markWatchedBusy = false,
                        showError = true,
                        errorMessage = ex.localizedMessage ?: "Unknown Error"
                    )
                }
            }
        }
    }

    fun addToPlaylist() {

    }

    fun manageParentalControls() {

    }

    fun navToEpisodeInfo(id: Int) {

    }
}