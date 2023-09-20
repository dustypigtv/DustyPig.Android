package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.Notification
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.AuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
): _RepositoryBase(authManager)  {

    suspend fun list(): List<Notification> = wrapAPICallWithReturnData { apiService.listNotifications() }

    suspend fun delete(id: Int) = wrapAPICall { apiService.deleteNotification(id) }

    suspend fun markAsRead(id: Int) = wrapAPICall { apiService.markNotificationRead(id) }
}