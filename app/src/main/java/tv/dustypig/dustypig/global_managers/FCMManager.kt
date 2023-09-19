package tv.dustypig.dustypig.global_managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import tv.dustypig.dustypig.MainActivity
import tv.dustypig.dustypig.R
import kotlin.random.Random


class FCMManager: FirebaseMessagingService() {

    companion object {

        private const val TAG = "FCMManager"
        private const val CHANNEL_NAME = "Notifications"

        private val random = Random

        var currentToken: String = ""
            private set
        }

    init {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            currentToken = task.result
            Log.d(TAG, "Current token: $currentToken")

        })
    }


    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        currentToken = token

    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        //This is only called when the app is in the foreground.
        //Maybe instead of sending to the NotificationManager,
        //we display an in-app dialog

        Log.d(TAG, "onMessageReceived")
        remoteMessage.notification?.let { message ->
            sendNotification(message)
        }
    }

    private fun sendNotification(message: RemoteMessage.Notification) {

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, FLAG_IMMUTABLE
        )

        val channelId = this.getString(R.string.default_notification_channel_id)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(Color.BLACK)
            .setColorized(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, CHANNEL_NAME, IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        manager.notify(random.nextInt(), notificationBuilder.build())
    }

}