package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.AuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibrariesRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
) : RepositoryBase(authManager) {

    suspend fun adminList() = wrapAPICallWithReturnData { apiService.adminListLibraries() }

    suspend fun list() = wrapAPICallWithReturnData { apiService.listLibraries() }
}