package tv.dustypig.dustypig.global_managers.fcm_manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import tv.dustypig.dustypig.MainActivity
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.NotificationsManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics


class FCMManager: FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMManager"

        const val DATA_ID = "id"
        const val DATA_TITLE = "title"
        const val DATA_MESSAGE = "message"
        const val DATA_DEEP_LINK = "deeplink"
        const val DATA_PROFILE_ID = "profileid"

        private const val INTENT_PREFIX = "tv.dustypig.dustypig"
        const val INTENT_DATA_ID = "${INTENT_PREFIX}.notification_id"
        const val INTENT_DATA_DEEP_LINK = "${INTENT_PREFIX}.notification_deep_link"

        private const val CHANNEL_NAME = "Notifications"

        private var _activityCount = 0

        private var _nextAlertId: Int = 0
        private val _inAppAlertFlow = MutableSharedFlow<FCMAlertData>(replay = 1)

        var currentToken: String = ""
            private set


        fun init() {
            if (_activityCount == 0) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }
                    currentToken = task.result
                    Log.d(TAG, "Current token: $currentToken")
                })
            }
        }

        fun activityResumed() {
            _activityCount++
        }

        fun activityPaused() {
            _activityCount--
        }
    }


    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        currentToken = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(TAG, "onMessageReceived")

        try {
            val settingsManager = SettingsManager(applicationContext)

            val currentProfileId = runBlocking { settingsManager.getProfileId() }
            if(currentProfileId == 0)
                return

            val targetProfileId = remoteMessage.data[DATA_PROFILE_ID]!!.toInt()
            if(targetProfileId != currentProfileId)
                return

            NotificationsManager.triggerUpdate()
            val allowed = runBlocking { settingsManager.getAllowNotifications() }
            if(!allowed) {
                return
            }

            if (_activityCount > 0)
                addAlert(remoteMessage)
            else
                sendNotification(remoteMessage)

        } catch (ex: Exception) {
            ex.logToCrashlytics()
            ex.printStackTrace()
        }
    }


    private fun addAlert(remoteMessage: RemoteMessage) {
        _inAppAlertFlow.tryEmit(
            FCMAlertData(
                id = remoteMessage.data[DATA_ID]!!.toInt(),
                title = remoteMessage.data[DATA_TITLE]!!,
                message = remoteMessage.data[DATA_MESSAGE]!!,
                deepLink = remoteMessage.data[DATA_DEEP_LINK]
            )
        )
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_FROM_BACKGROUND or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        intent.putExtra(INTENT_DATA_ID, remoteMessage.data[DATA_ID]!!.toInt())
        intent.putExtra(INTENT_DATA_DEEP_LINK, remoteMessage.data[DATA_DEEP_LINK])

        val pendingIntent = PendingIntent.getActivity(
            this, 1, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        val channelId = this.getString(R.string.default_notification_channel_id)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(remoteMessage.data[DATA_TITLE])
            .setContentText(remoteMessage.data[DATA_MESSAGE])
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(Color.BLACK)
            .setAutoCancel(true)
            //.setSound(Uri.parse("android.resource://tv.dustypig.dustypig/" + R.raw.oink))
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(_nextAlertId++, notificationBuilder.build())
    }
}
