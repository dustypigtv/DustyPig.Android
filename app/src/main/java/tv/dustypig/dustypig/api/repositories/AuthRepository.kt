package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.models.FCMToken
import tv.dustypig.dustypig.api.models.StringValue
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.auth_manager.AuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
) : RepositoryBase(authManager) {

    suspend fun loginDeviceWithCode(code: StringValue) =
        wrapAPICall { apiService.loginDeviceWithCode(code) }

    suspend fun passwordLogin(passwordCredentials: PasswordCredentials) =
        wrapAPICallWithReturnData { apiService.passwordLogin(passwordCredentials) }

    suspend fun sendPasswordResetEmail(email: StringValue) =
        wrapAPICall { apiService.sendPasswordResetEmail(email) }

    suspend fun profileLogin(profileCredentials: ProfileCredentials) =
        wrapAPICallWithReturnData { apiService.profileLogin(profileCredentials) }

    suspend fun signout() = wrapAPICall { apiService.signout() }

    suspend fun signoutEverywhere() = wrapAPICall { apiService.signoutEverywhere() }

    suspend fun updateFCMToken(fcmToken: FCMToken) =
        wrapAPICallWithReturnData { apiService.updateFCMToken(fcmToken) }
}