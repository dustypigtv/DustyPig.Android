package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.TitleRequest
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.AuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TMDBRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
): RepositoryBase(authManager) {

    suspend fun getMovie(id: Int) = wrapAPICallWithReturnData { apiService.getTMDBMovie(id) }

    suspend fun getSeries(id: Int) = wrapAPICallWithReturnData { apiService.getTMDBSeries(id) }

    suspend fun requestTitle(titleRequest: TitleRequest) = wrapAPICall { apiService.requestTMDBTitle(titleRequest) }

    suspend fun cancelTitleRequest(titleRequest: TitleRequest) = wrapAPICall { apiService.cancelTMDBTitleRequest(titleRequest) }

    suspend fun getPerson(id: Int) = wrapAPICallWithReturnData { apiService.getTMDBPerson(id) }
}