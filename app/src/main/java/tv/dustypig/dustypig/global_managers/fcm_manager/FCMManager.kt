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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.dustypig.dustypig.MainActivity
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.logToCrashlytics



class FCMManager: FirebaseMessagingService() {

    companion object {

        const val DATA_ID = "id"
        const val DATA_TITLE = "title"
        const val DATA_MESSAGE = "message"
        const val DATA_DEEP_LINK = "deeplink"

        private const val TAG = "FCMManager"
        private const val CHANNEL_NAME = "Notifications"

        private var _activityCount = 0

        private var _nextAlertId: Int = 0
        private val _inAppAlertFlow = MutableStateFlow<FCMAlertData?>(null)

        val inAppAlerts = _inAppAlertFlow.asStateFlow()

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
        intent.putExtra(DATA_ID, remoteMessage.data[DATA_ID]!!.toInt())
        intent.putExtra(DATA_DEEP_LINK, remoteMessage.data[DATA_DEEP_LINK])

        val pendingIntent = PendingIntent.getActivity(
            this, 1, intent, PendingIntent.FLAG_IMMUTABLE
        )


        val channelId = this.getString(R.string.default_notification_channel_id)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(remoteMessage.data[DATA_TITLE])
            .setContentText(remoteMessage.data[DATA_MESSAGE])
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(Color.BLACK)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(_nextAlertId++, notificationBuilder.build())
    }
}