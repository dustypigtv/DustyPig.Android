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
): RepositoryBase(authManager)  {

    suspend fun list(): List<Notification> {
        val ret = arrayListOf<Notification>()
        while (true) {
            //The call returns 100 notifications at once
            val nextLst = wrapAPICallWithReturnData { apiService.listNotifications(ret.count()) }
            ret.addAll(nextLst)
            if(nextLst.count() < 100)
                return ret
        }
    }

    suspend fun delete(id: Int) = wrapAPICall { apiService.deleteNotification(id) }

    suspend fun markAsRead(id: Int) = wrapAPICall { apiService.markNotificationRead(id) }
}