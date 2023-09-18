package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.di.UnAuthenticatedAPIService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    @UnAuthenticatedAPIService private val unAuthenticatedApiService: ApiService,
    authManager: AuthManager
): _RepositoryBase(authManager) {

    suspend fun create(createAccount: CreateAccount) = wrapAPICallWithReturnData { unAuthenticatedApiService.createAccount(createAccount) }
}