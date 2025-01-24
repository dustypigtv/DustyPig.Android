package tv.dustypig.dustypig.ui.main_app.screens.series_details

import androidx.annotation.OptIn
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.GenrePair
import tv.dustypig.dustypig.api.models.Genres
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.api.repositories.SeriesRepository
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.media_cache_manager.MediaCacheManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title.ManageParentalControlsForTitleNav
import tv.dustypig.dustypig.ui.main_app.screens.person_details.PersonDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import tv.dustypig.dustypig.ui.main_app.screens.show_more.ShowMoreNav
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SeriesDetailsViewModel @OptIn(UnstableApi::class) @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val seriesRepository: SeriesRepository,
    private val downloadManager: DownloadManager,
    routeNavigator: RouteNavigator,
    castManager: CastManager,
    savedStateHandle: SavedStateHandle
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        SeriesDetailsUIState(
            castManager = castManager,
            onAddToPlaylist = ::addToPlaylist,
            onRequestAccess = ::requestAccess,
            onMarkWatched = ::markWatched,
            onManagePermissions = ::managePermissions,
            onPlay = ::playUpNext,
            onPlayEpisode = ::playEpisode,
            onToggleWatchList = ::toggleWatchList,
            onUpdateDownload = ::updateDownloads,
            onHideError = ::hideError,
            onPopBackStack = ::popBackStack,
            onNavToEpisodeInfo = ::navToEpisodeInfo,
            onSelectSeason = ::selectSeason,
            onToggleSubscribe = ::toggleSubscribe
        )
    )
    val uiState: StateFlow<SeriesDetailsUIState> = _uiState.asStateFlow()

    private val _basicCacheId: String =
        savedStateHandle.getOrThrow(SeriesDetailsNav.KEY_BASIC_CACHE_ID)
    private val _mediaId: Int = savedStateHandle.getOrThrow(SeriesDetailsNav.KEY_MEDIA_ID)

    private var _detailedSeries = DetailedSeries()
    private val _detailCacheId = UUID.randomUUID().toString()

    init {
        val cachedInfo = MediaCacheManager.getBasicInfo(_basicCacheId)
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

        viewModelScope.launch {
            downloadManager.downloads.collectLatest { jobLst ->
                val job = jobLst.firstOrNull {
                    it.mediaId == _mediaId && it.mediaType == MediaTypes.Series
                }
                if (job == null) {
                    _uiState.update {
                        it.copy(
                            downloadStatus = DownloadStatus.None,
                            currentDownloadCount = 0
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            downloadStatus = job.status,
                            currentDownloadCount = job.count
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        MediaCacheManager.BasicInfo.removeAll { it.cacheId == _basicCacheId }
        MediaCacheManager.Series.remove(_detailCacheId)
    }

    private suspend fun updateData() {
        _uiState.update {
            it.copy(
                title = MediaCacheManager.getBasicInfo(_basicCacheId).title
            )
        }

        try {
            _detailedSeries = seriesRepository.details(_mediaId)
            _detailedSeries.episodes?.forEach { detailedEpisode ->
                detailedEpisode.seriesTitle = _detailedSeries.title
            }
            MediaCacheManager.Series[_detailCacheId] = _detailedSeries

            val episodes = _detailedSeries.episodes ?: listOf()
            if (episodes.isEmpty()) {
                throw Exception("No episodes found.")
            }

            val allSeasons = ArrayList<UShort>()
            for (ep in episodes) {
                if (!allSeasons.contains(ep.seasonNumber))
                    allSeasons.add(ep.seasonNumber)
            }

            val upNext: DetailedEpisode = episodes.firstOrNull { it.upNext } ?: episodes.first()


            val unPlayed =
                upNext.id == episodes.first().id && (upNext.played == null || upNext.played < 1)
            val fullyPlayed = upNext.id == episodes.last().id && (upNext.played
                ?: 0.0) >= (upNext.creditsStartTime ?: (upNext.length - 30.0))

            _uiState.update {
                it.copy(
                    loading = false,
                    posterUrl = _detailedSeries.artworkUrl,
                    backdropUrl = _detailedSeries.backdropUrl ?: "",
                    seasons = allSeasons.toList(),
                    selectedSeason = upNext.seasonNumber,
                    episodes = episodes,
                    creditsData = CreditsData(
                        genres = Genres(_detailedSeries.genres).toList(),
                        genreNav = ::genreNav,
                        castAndCrew = _detailedSeries.credits ?: listOf(),
                        personNav = ::personNav,
                        owner = _detailedSeries.owner ?: ""
                    ),
                    inWatchList = _detailedSeries.inWatchlist,
                    title = _detailedSeries.title,
                    canManage = _detailedSeries.canManage,
                    canPlay = _detailedSeries.canPlay,
                    rated = _detailedSeries.rated.toString(),
                    overview = (if (unPlayed) _detailedSeries.description else upNext.description)
                        ?: "",
                    partiallyPlayed = !(unPlayed || fullyPlayed),
                    upNextSeason = if (unPlayed) null else upNext.seasonNumber,
                    upNextEpisode = if (unPlayed) null else upNext.episodeNumber,
                    episodeTitle = if (unPlayed) "" else upNext.title,
                    titleRequestPermissions = _detailedSeries.titleRequestPermission,
                    accessRequestStatus = _detailedSeries.accessRequestedStatus,
                    accessRequestBusy = false,
                    subscribed = _detailedSeries.subscribed
                )
            }
        } catch (ex: Exception) {
            setError(ex = ex, criticalError = true)
        }
    }

    private fun setError(ex: Exception, criticalError: Boolean) {
        ex.logToCrashlytics()
        _uiState.update {
            it.copy(
                loading = false,
                showErrorDialog = true,
                errorMessage = ex.localizedMessage,
                criticalError = criticalError,
                accessRequestBusy = false,
                watchListBusy = false,
                markWatchedBusy = false,
                subscribeBusy = false
            )
        }
    }

    private fun hideError() {
        if (_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }

    private fun playUpNext() {
        navigateToRoute(
            PlayerNav.getRoute(
                mediaId = _detailedSeries.id,
                sourceType = PlayerNav.MEDIA_TYPE_SERIES,
                upNextId = -1
            )
        )
    }

    private fun playEpisode(id: Int) {
        navigateToRoute(
            PlayerNav.getRoute(
                mediaId = _detailedSeries.id,
                sourceType = PlayerNav.MEDIA_TYPE_SERIES,
                upNextId = id
            )
        )
    }

    private fun updateDownloads(newCount: Int) {
        viewModelScope.launch {
            downloadManager.addOrUpdateSeries(_detailedSeries, newCount)
        }
    }

    private fun requestAccess() {
        _uiState.update {
            it.copy(accessRequestBusy = true)
        }

        viewModelScope.launch {
            try {
                mediaRepository.requestAccessOverride(_mediaId)
                _uiState.update {
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
        _uiState.update {
            it.copy(watchListBusy = true)
        }

        viewModelScope.launch {

            try {

                if (_uiState.value.inWatchList) {
                    mediaRepository.deleteFromWatchlist(_mediaId)
                } else {
                    mediaRepository.addToWatchlist(_mediaId)
                }

                _uiState.update {
                    it.copy(
                        watchListBusy = false,
                        inWatchList = it.inWatchList.not()
                    )
                }

                HomeViewModel.triggerUpdate()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

    private fun markWatched(removeFromContinueWatching: Boolean) {
        _uiState.update {
            it.copy(markWatchedBusy = true)
        }
        viewModelScope.launch {
            try {
                if (removeFromContinueWatching) {
                    seriesRepository.removeFromContinueWatching(_mediaId)
                } else {
                    seriesRepository.markWatched(_mediaId)
                }
                _uiState.update {
                    it.copy(
                        markWatchedBusy = false,
                        partiallyPlayed = false
                    )
                }
                updateData()
                HomeViewModel.triggerUpdate()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }


    private fun addToPlaylist() {
        navigateToRoute(AddToPlaylistNav.getRouteForId(_mediaId, true))
    }

    private fun managePermissions() {
        navigateToRoute(ManageParentalControlsForTitleNav.getRouteForId(_mediaId))
    }

    private fun navToEpisodeInfo(id: Int) {
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
                basicCacheId = cacheId,
                detailedCacheId = _detailCacheId,
                canPlay = _detailedSeries.canPlay,
                fromSeriesDetails = true,
                playlistUpNextIndex = 0
            )
        )
    }

    private fun toggleSubscribe() {
        _uiState.update {
            it.copy(
                subscribeBusy = true
            )
        }

        viewModelScope.launch {
            try {
                if (_uiState.value.subscribed) {
                    seriesRepository.unsubscribe(_mediaId)
                    _uiState.update {
                        it.copy(
                            subscribeBusy = false,
                            subscribed = false
                        )
                    }
                } else {
                    seriesRepository.subscribe(_mediaId)
                    _uiState.update {
                        it.copy(
                            subscribeBusy = false,
                            subscribed = true
                        )
                    }
                }
            } catch (ex: Exception) {
                setError(ex, false)
            }
        }
    }

    /**
     * Put this here to survive recomposition when returning from navigation
     */
    private fun selectSeason(seasonNumber: UShort) {
        _uiState.update {
            it.copy(
                selectedSeason = seasonNumber
            )
        }
    }

    private fun genreNav(genrePair: GenrePair) {
        navigateToRoute(ShowMoreNav.getRoute(genrePair.genre.value, genrePair.text))
    }

    private fun personNav(tmdbId: Int, cacheId: String) {
        navigateToRoute(PersonDetailsNav.getRoute(tmdbId, cacheId))
    }
}