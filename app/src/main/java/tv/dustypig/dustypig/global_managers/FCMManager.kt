package tv.dustypig.dustypig.global_managers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking
import tv.dustypig.dustypig.MainActivity
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics


class FCMManager : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMManager"

        private const val CHANNEL_NAME = "Notifications"

        var currentToken: String = ""
            private set


        fun init() {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                currentToken = task.result
                Log.d(TAG, "Current token: $currentToken")
            })
        }

        fun resetToken() {
            Log.d(TAG, "Current token before reset $currentToken")
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Deleting FCM Token succeeded")
                    init()
                } else {
                    Log.w(TAG, "Deleting FCM Token failed", task.exception)
                }
            }
        }
    }


    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        currentToken = token
        AlertsManager.triggerUpdateFCMToken()
    }

    @OptIn(UnstableApi::class)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // This method is only called when the app is in the foreground
        Log.d(TAG, "onMessageReceived")

        try {
            val settingsManager = SettingsManager(applicationContext)

            val currentProfileId = runBlocking { settingsManager.getProfileId() }
            if (currentProfileId == 0)
                return

            val targetProfileId = remoteMessage.data[AlertsManager.DATA_PROFILE_ID]!!.toInt()
            if (targetProfileId != currentProfileId)
                return

            val allowed = runBlocking { settingsManager.getAllowNotifications() }
            if (!allowed) {
                return
            }

//            if (!PlayerStateManager.playerScreenVisible.value) {
            if(!PlayerStateManager.playerScreenVisible) {

                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_FROM_BACKGROUND or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }

                remoteMessage.data.forEach { (t, u) ->
                    intent.putExtra(t, u)
                }

                val pendingIntent = PendingIntent.getActivity(
                    this,
                    1,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val channelId = this.getString(R.string.default_notification_channel_id)

                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setContentTitle(remoteMessage.notification?.title)
                    .setContentText(remoteMessage.notification?.body)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(Color.BLACK)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(remoteMessage.notification?.body)
                    )
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationChannel =
                        NotificationChannel(
                            channelId,
                            CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_HIGH
                        )
                    notificationChannel.setShowBadge(true)
                    notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    notificationManager.createNotificationChannel(notificationChannel)
                }
                notificationManager.notify(
                    remoteMessage.data[AlertsManager.DATA_ID]!!.toInt(),
                    notificationBuilder.build()
                )
            }
        } catch (ex: Exception) {
            ex.logToCrashlytics()
        }
    }
}