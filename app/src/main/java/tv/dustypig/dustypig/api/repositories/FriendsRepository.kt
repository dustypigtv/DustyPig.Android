package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.SimpleValue
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.AuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendsRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
): _RepositoryBase(authManager) {

    suspend fun list() = wrapAPICallWithReturnData { apiService.listFriends() }

    suspend fun invite(email: String) = wrapAPICall { apiService.inviteFriend(SimpleValue(email)) }
}