package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.auth_manager.AuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeriesRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
) : RepositoryBase(authManager) {

    suspend fun details(id: Int) = wrapAPICallWithReturnData { apiService.seriesDetails(id) }

    suspend fun markWatched(id: Int) = wrapAPICall { apiService.markSeriesWatched(id) }

    suspend fun removeFromContinueWatching(id: Int) =
        wrapAPICall { apiService.removeFromContinueWatching(id) }

    suspend fun subscribe(id: Int) = wrapAPICall { apiService.subscribeToSeries(id) }

    suspend fun unsubscribe(id: Int) = wrapAPICall { apiService.unsubscribeFromSeries(id) }

}