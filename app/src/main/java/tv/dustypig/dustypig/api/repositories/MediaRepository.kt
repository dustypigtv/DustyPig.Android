package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.LoadMoreHomeScreenItemsRequest
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.models.SearchRequest
import tv.dustypig.dustypig.api.models.SetTitlePermissionInfo
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.AuthManager
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MediaRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
): _RepositoryBase(authManager) {

    suspend fun addToWatchlist(id: Int) = wrapAPICall { apiService.addToWatchlist(id) }

    suspend fun deleteFromWatchlist(id: Int) = wrapAPICall { apiService.deleteFromWatchlist(id) }

    suspend fun getTitlePermissions(id: Int) = wrapAPICallWithReturnData { apiService.getTitlePermissions(id) }

    suspend fun homeScreen() = wrapAPICallWithReturnData { apiService.homeScreen() }

    suspend fun loadMoreHomeScreenItems(loadMoreHomeScreenListItemsRequest: LoadMoreHomeScreenItemsRequest) =
        wrapAPICallWithReturnData { apiService.loadMoreHomeScreenItems(loadMoreHomeScreenListItemsRequest) }

    suspend fun requestAccessOverride(id: Int) = wrapAPICall { apiService.requestAccessOverride(id) }

    suspend fun search(searchRequest: SearchRequest)= wrapAPICallWithReturnData { apiService.search(searchRequest) }

    suspend fun setTitlePermissions(setTitlePermissionInfo: SetTitlePermissionInfo) = wrapAPICall { apiService.setTitlePermissions(setTitlePermissionInfo) }

    suspend fun updatePlaybackProgress(playbackProgress: PlaybackProgress) = wrapAPICall { apiService.updatePlaybackProgress(playbackProgress) }
}