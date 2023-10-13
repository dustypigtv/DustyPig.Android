package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.LibraryFriendLink
import tv.dustypig.dustypig.api.models.SimpleValue
import tv.dustypig.dustypig.api.models.UpdateFriend
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.AuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendsRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
): RepositoryBase(authManager) {

    suspend fun list() = wrapAPICallWithReturnData { apiService.listFriends() }

    suspend fun invite(email: String) = wrapAPICall { apiService.inviteFriend(SimpleValue(email)) }

    suspend fun details(id: Int) = wrapAPICallWithReturnData { apiService.friendDetails(id) }

    suspend fun update(updateFriend: UpdateFriend) = wrapAPICall { apiService.updateFriend(updateFriend) }

    suspend fun shareLibrary(libraryFriendLink: LibraryFriendLink) = wrapAPICall { apiService.shareLibraryWithFriend(libraryFriendLink) }

    suspend fun unShareLibrary(libraryFriendLink: LibraryFriendLink) = wrapAPICall { apiService.unShareLibraryWithFriend(libraryFriendLink) }
}