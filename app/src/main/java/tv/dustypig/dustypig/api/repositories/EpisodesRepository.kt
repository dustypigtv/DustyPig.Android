package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.auth_manager.AuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodesRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
) : RepositoryBase(authManager) {

    suspend fun details(id: Int) = wrapAPICallWithReturnData { apiService.episodeDetails(id) }

}