package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.di.UnAuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.auth_manager.AuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    @UnAuthenticatedAPIService private val unAuthenticatedApiService: ApiService,
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
) : RepositoryBase(authManager) {

    suspend fun create(createAccount: CreateAccount) =
        wrapAPICall { unAuthenticatedApiService.createAccount(createAccount) }

    suspend fun changePassword(newPassword: String) =
        wrapAPICall { apiService.changePassword(newPassword) }

    suspend fun delete() = wrapAPICall { apiService.deleteAccount() }
}