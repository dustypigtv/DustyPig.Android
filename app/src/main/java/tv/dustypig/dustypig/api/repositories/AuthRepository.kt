package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.models.SimpleValue
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
): _RepositoryBase(authManager) {

    suspend fun passwordLogin(passwordCredentials: PasswordCredentials) = wrapAPICallWithReturnData { apiService.passwordLogin(passwordCredentials) }

    suspend fun sendPasswordResetEmail(email: String) = wrapAPICall { apiService.sendPasswordResetEmail(SimpleValue(email)) }

    suspend fun profileLogin(profileCredentials: ProfileCredentials) = wrapAPICallWithReturnData { apiService.profileLogin(profileCredentials) }
}