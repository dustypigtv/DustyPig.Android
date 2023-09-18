package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeriesRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
): _RepositoryBase(authManager) {

    suspend fun details(id: Int) = wrapAPICallWithReturnData { apiService.seriesDetails(id) }

    suspend fun markWatched(id: Int) = wrapAPICall { apiService.markSeriesWatched(id) }

    suspend fun removeFromContinueWatching(id: Int) = wrapAPICall { apiService.removeFromContinueWatching(id) }

}