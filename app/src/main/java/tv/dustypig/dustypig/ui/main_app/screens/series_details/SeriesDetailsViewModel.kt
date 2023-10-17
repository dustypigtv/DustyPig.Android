package tv.dustypig.dustypig.ui.main_app.screens.series_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.Genres
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.api.repositories.SeriesRepository
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.media_cache_manager.MediaCacheManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.main_app.DetailsScreenBaseViewModel
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title.ManageParentalControlsForTitleNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerViewModel
import javax.inject.Inject

@HiltViewModel
class SeriesDetailsViewModel  @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val seriesRepository: SeriesRepository,
    routeNavigator: RouteNavigator,
    downloadManager: DownloadManager,
    savedStateHandle: SavedStateHandle
): DetailsScreenBaseViewModel(routeNavigator, downloadManager, MediaTypes.Series) {



    private val _uiState = MutableStateFlow(SeriesDetailsUIState())
    val uiState: StateFlow<SeriesDetailsUIState> = _uiState.asStateFlow()

    private val _cacheId: String = savedStateHandle.getOrThrow(SeriesDetailsNav.KEY_CACHE_ID)
    override val mediaId: Int = savedStateHandle.getOrThrow(SeriesDetailsNav.KEY_MEDIA_ID)
    private lateinit var _detailedSeries: DetailedSeries

    init {
        val cachedInfo = MediaCacheManager.get(_cacheId)
        _uiState.update {
            it.copy(
                posterUrl = cachedInfo.posterUrl,
                backdropUrl = cachedInfo.backdropUrl ?: ""
            )
        }

        viewModelScope.launch {
            PlayerStateManager.playbackEnded.collectLatest {
                updateData()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        MediaCacheManager.remove(_cacheId)
    }

    private fun updateData() {
        baseTitleInfoUIState.update {
            it.copy(
                title = MediaCacheManager.get(_cacheId).title,
                mediaType = MediaTypes.Series,
                playClick = ::playUpNext,
                toggleWatchList = ::toggleWatchList,
                updateDownload = ::updateDownloads,
                addToPlaylist = ::addToPlaylist,
                markSeriesWatched = ::markWatched,
                requestAccess = ::requestAccess,
                manageClick = ::manageParentalControls,
            )
        }

        viewModelScope.launch {
            try {
                _detailedSeries = seriesRepository.details(mediaId)
                val episodes = _detailedSeries.episodes ?: listOf()
                if(episodes.isEmpty()) {
                    throw Exception("No episodes found.")
                }

                val allSeasons = ArrayList<UShort>()
                for(ep in episodes) {
                    if(!allSeasons.contains(ep.seasonNumber))
                        allSeasons.add(ep.seasonNumber)
                }

                val upNext: DetailedEpisode = episodes.firstOrNull { it.upNext } ?: episodes.first()


                val unPlayed = upNext.id == episodes.first().id && (upNext.played == null || upNext.played < 1)
                val fullyPlayed = upNext.id == episodes.last().id && (upNext.played ?: 0.0) >= (upNext.creditStartTime ?: (upNext.length - 30.0))

                _uiState.update {
                    it.copy(
                        loading = false,
                        posterUrl = _detailedSeries.artworkUrl,
                        backdropUrl = _detailedSeries.backdropUrl ?: "",
                        seasons = allSeasons.toList(),
                        upNextSeason = upNext.seasonNumber,
                        selectedSeason = upNext.seasonNumber,
                        episodes = episodes,
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

                baseTitleInfoUIState.update {
                    it.copy(
                        inWatchList = _detailedSeries.inWatchlist,
                        title = _detailedSeries.title,
                        canManage = _detailedSeries.canManage,
                        canPlay = _detailedSeries.canPlay,
                        rated = _detailedSeries.rated.toString(),
                        overview = (if(unPlayed) _detailedSeries.description else upNext.description) ?: "",
                        partiallyPlayed = !(unPlayed || fullyPlayed),
                        upNextSeason = if(unPlayed) null else upNext.seasonNumber,
                        upNextEpisode = if(unPlayed) null else upNext.episodeNumber,
                        episodeTitle = if(unPlayed) "" else upNext.title,
                        titleRequestPermissions = _detailedSeries.titleRequestPermissions,
                        accessRequestStatus = _detailedSeries.accessRequestStatus,
                        accessRequestBusy = false,
                        mediaId = mediaId
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = true)
            }
        }
    }

    private fun setError(ex: Exception, criticalError: Boolean) {
        ex.logToCrashlytics()
        _uiState.update {
            it.copy(
                loading = false,
                showErrorDialog = true,
                errorMessage = ex.localizedMessage,
                criticalError = criticalError
            )
        }
        baseTitleInfoUIState.update {
            it.copy(
                accessRequestBusy = false,
                watchListBusy = false,
                markWatchedBusy = false
            )
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


    private fun playUpNext() {
        val episodes = _detailedSeries.episodes ?: listOf()
        val upNext: DetailedEpisode = episodes.firstOrNull { it.upNext } ?: episodes.first()
        PlayerViewModel.mediaType = MediaTypes.Series
        PlayerViewModel.detailedSeries = _detailedSeries
        PlayerViewModel.upNextId = upNext.id
        navigateToRoute(PlayerNav.route)
    }

    fun playEpisode(id: Int) {
        PlayerViewModel.mediaType = MediaTypes.Series
        PlayerViewModel.detailedSeries = _detailedSeries
        PlayerViewModel.upNextId = id
        navigateToRoute(PlayerNav.route)
    }

    private fun updateDownloads(newCount: Int) {
       viewModelScope.launch {
            if (newCount == 0)
                downloadManager.delete(_detailedSeries.id, MediaTypes.Series)
            else
                downloadManager.addOrUpdateSeries(_detailedSeries, newCount)
        }
    }


    private fun requestAccess() {
        baseTitleInfoUIState.update {
            it.copy(accessRequestBusy = true)
        }

        viewModelScope.launch {
            try{
                mediaRepository.requestAccessOverride(mediaId)
                baseTitleInfoUIState.update {
                    it.copy(
                        accessRequestBusy = false,
                        accessRequestStatus = OverrideRequestStatus.Requested
                    )
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun toggleWatchList() {
        baseTitleInfoUIState.update {
            it.copy(watchListBusy = true)
        }

        viewModelScope.launch {

            try {

                if(baseTitleInfoUIState.value.inWatchList) {
                    mediaRepository.deleteFromWatchlist(mediaId)
                } else {
                    mediaRepository.addToWatchlist(mediaId)
                }

                baseTitleInfoUIState.update {
                    it.copy(
                        watchListBusy = false,
                        inWatchList = baseTitleInfoUIState.value.inWatchList.not()
                    )
                }

                HomeViewModel.triggerUpdate()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun markWatched(removeFromContinueWatching: Boolean) {
        baseTitleInfoUIState.update {
            it.copy(markWatchedBusy = true)
        }
        viewModelScope.launch {
            try{
                if(removeFromContinueWatching) {
                    seriesRepository.removeFromContinueWatching(mediaId)
                } else {
                    seriesRepository.markWatched(mediaId)
                }
                baseTitleInfoUIState.update {
                    it.copy(
                        markWatchedBusy = false,
                        partiallyPlayed = false
                    )
                }
                HomeViewModel.triggerUpdate()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
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
        val episode = _detailedSeries.episodes?.firstOrNull {
            it.id == id
        } ?: return

        val cacheId = MediaCacheManager.add(
            title = episode.title,
            posterUrl = _detailedSeries.artworkUrl,
            backdropUrl = episode.artworkUrl
        )
        navigateToRoute(
            EpisodeDetailsNav.getRoute(
                mediaId = id,
                cacheId = cacheId,
                canPlay = _detailedSeries.canPlay,
                fromSeriesDetails = true
            )
        )
    }

    /**
     * Put this here to survive recomposition when returning from navigation
     */
    fun selectSeason(seasonNumber: UShort) {
        _uiState.update {
            it.copy(
                selectedSeason = seasonNumber
            )
        }
    }
}