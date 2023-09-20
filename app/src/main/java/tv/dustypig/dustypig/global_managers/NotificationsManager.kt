package tv.dustypig.dustypig.global_managers

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.Notification
import tv.dustypig.dustypig.api.repositories.NotificationsRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@OptIn(DelicateCoroutinesApi::class)
class NotificationsManager @Inject constructor(
    private val notificationsRepository: NotificationsRepository
) {
    private val _notificationsFlow = MutableSharedFlow<List<Notification>>(replay = 1)
    val notifications = _notificationsFlow.asSharedFlow()

    fun markAsRead(id: Int) {
        GlobalScope.launch {
            try {
                notificationsRepository.markAsRead(id)
                val lst = notificationsRepository.list()
                _notificationsFlow.tryEmit(lst)

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun delete(id: Int) {
        GlobalScope.launch {
            try {
                notificationsRepository.delete(id)
                val lst = notificationsRepository.list()
                _notificationsFlow.tryEmit(lst)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

}