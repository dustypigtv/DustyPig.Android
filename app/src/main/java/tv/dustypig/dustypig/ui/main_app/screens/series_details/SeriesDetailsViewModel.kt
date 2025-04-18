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
import tv.dustypig.dustypig.global_managers.ArtworkCache
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.global_managers.download_manager.MyDownloadManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import tv.dustypig.dustypig.ui.composables.CreditsData
import tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist.AddToPlaylistNav
import tv.dustypig.dustypig.ui.main_app.screens.episode_details.EpisodeDetailsNav
import tv.dustypig.dustypig.ui.main_app.screens.home.HomeViewModel
import tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title.ManageParentalControlsForTitleNav
import tv.dustypig.dustypig.ui.main_app.screens.player.PlayerNav
import tv.dustypig.dustypig.ui.main_app.screens.show_more.ShowMoreNav
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class SeriesDetailsViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val seriesRepository: SeriesRepository,
    private val downloadManager: MyDownloadManager,
    routeNavigator: RouteNavigator,
    castManager: CastManager,
    savedStateHandle: SavedStateHandle
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _mediaId: Int = savedStateHandle.getOrThrow(SeriesDetailsNav.KEY_MEDIA_ID)

    private val _cachedPoster = ArtworkCache.getMediaPoster(_mediaId) ?: ""
    private val _cachedBackdrop = ArtworkCache.getMediaBackdrop(_mediaId) ?: ""

    private val _uiState = MutableStateFlow(
        SeriesDetailsUIState(
            posterUrl = _cachedPoster,
            backdropUrl = _cachedBackdrop,
            loading = true,
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

    private var _detailedSeries: DetailedSeries? = null

    init {

        viewModelScope.launch {
            PlayerStateManager.playbackEnded.collectLatest {
                updateData()
            }
        }

        viewModelScope.launch {
            downloadManager.currentDownloads.collectLatest { jobLst ->
                var status = DownloadStatus.None
                var percent = 0f
                var cnt = 0

                val job = jobLst.firstOrNull {
                    it.mediaId == _mediaId && it.mediaType == MediaTypes.Series
                }
                if (job != null) {
                    status = job.status
                    cnt = job.count
                    percent = when (cnt) {
                        0 -> 1f
                        else -> job.downloads.sumOf { it.percent.toDouble() }.toFloat() / cnt
                    }
                }
                _uiState.update {
                    it.copy(
                        downloadStatus = status,
                        currentDownloadCount = cnt,
                        downloadPercent = percent
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ArtworkCache.deleteMedia(_mediaId)
    }


    private suspend fun updateData() {

        try {
            _detailedSeries = seriesRepository.details(_mediaId)

            _detailedSeries!!.episodes?.forEach { detailedEpisode ->
                detailedEpisode.seriesTitle = _detailedSeries!!.title
            }

            val episodes = _detailedSeries!!.episodes ?: listOf()
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
                upNext.id == episodes.first().id && (upNext.played == null || upNext.played!! < 1)
            val fullyPlayed = upNext.id == episodes.last().id && (upNext.played
                ?: 0.0) >= (upNext.creditsStartTime ?: (upNext.length - 30.0))

            //Prevent flicker
            if(_detailedSeries!!.artworkUrl != _cachedPoster ||
                _detailedSeries!!.backdropUrl != _cachedBackdrop) {
                _uiState.update {
                    it.copy(
                        posterUrl = _detailedSeries!!.artworkUrl,
                        backdropUrl = _detailedSeries!!.backdropUrl ?: ""
                    )
                }
            }

            _uiState.update {
                it.copy(
                    loading = false,
                    seasons = allSeasons.toList(),
                    selectedSeason = upNext.seasonNumber,
                    episodes = episodes,
                    creditsData = CreditsData(
                        genres = Genres(_detailedSeries!!.genres).toList(),
                        genreNav = ::genreNav,
                        castAndCrew = _detailedSeries!!.credits ?: listOf(),
                        routeNavigator = this,
                        owner = _detailedSeries!!.owner ?: ""
                    ),
                    inWatchList = _detailedSeries!!.inWatchlist,
                    title = _detailedSeries!!.title,
                    canManage = _detailedSeries!!.canManage,
                    canPlay = _detailedSeries!!.canPlay,
                    rated = _detailedSeries!!.rated.toString(),
                    overview = (if (unPlayed) _detailedSeries!!.description else upNext.description)
                        ?: "",
                    partiallyPlayed = !(unPlayed || fullyPlayed),
                    upNextSeason = if (unPlayed) null else upNext.seasonNumber,
                    upNextEpisode = if (unPlayed) null else upNext.episodeNumber,
                    episodeTitle = if (unPlayed) "" else upNext.title,
                    titleRequestPermissions = _detailedSeries!!.titleRequestPermission,
                    accessRequestStatus = _detailedSeries!!.accessRequestedStatus,
                    accessRequestBusy = false,
                    subscribed = _detailedSeries!!.subscribed
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
        val upNext =
            _detailedSeries!!.episodes?.firstOrNull{ it.upNext } ?:
            _detailedSeries!!.episodes?.firstOrNull()
        playEpisode(upNext!!.id)
    }

    private fun playEpisode(id: Int) {
        navigateToRoute(
            PlayerNav.getRoute(
                mediaId = _mediaId,
                sourceType = PlayerNav.MEDIA_TYPE_SERIES,
                upNextId = id
            )
        )
    }

    private fun updateDownloads(newCount: Int) {
        viewModelScope.launch {
            downloadManager.addOrUpdateSeries(_detailedSeries!!, newCount)
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
        val episode = _detailedSeries!!.episodes!!.firstOrNull { it.id == id }
        ArtworkCache.add(episode!!)
        navigateToRoute(
            EpisodeDetailsNav.getRoute(
                parentId = _mediaId,
                mediaId = id,
                canPlay = _detailedSeries!!.canPlay,
                source = EpisodeDetailsNav.SOURCE_SERIES_DETAILS,
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

}