package tv.dustypig.dustypig.global_managers.cast_manager

import android.content.Context
import com.google.android.gms.cast.LaunchOptions
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.MediaIntentReceiver
import com.google.android.gms.cast.framework.media.NotificationOptions
import tv.dustypig.dustypig.R


class CastOptionsProvider : OptionsProvider {

    companion object {
        fun receiverApplicationId(context: Context) = context.getString(R.string.cast_app_id)
    }

    override fun getCastOptions(context: Context): CastOptions {

        val notificationOptions = NotificationOptions
            .Builder()
            .setActions(listOf(
                MediaIntentReceiver.ACTION_SKIP_PREV,
                MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                MediaIntentReceiver.ACTION_SKIP_NEXT,
                MediaIntentReceiver.ACTION_STOP_CASTING
            ), listOf(0, 1, 2, 3).toIntArray())
            //.SetTargetActivityClassName(CastExpandedControlsActivity.ACTIVITY_NAME)
            .build()

        val mediaOptions = CastMediaOptions
            .Builder()
            .setNotificationOptions(notificationOptions)
            .setImagePicker(ImagePickerImpl())
            //.SetExpandedControllerActivityClassName(CastExpandedControlsActivity.ACTIVITY_NAME)
            .build()

        val launchOptions = LaunchOptions.Builder().build()

        return CastOptions
            .Builder()
            .setLaunchOptions(launchOptions)
            .setReceiverApplicationId(context.getString(R.string.cast_app_id))
            .setCastMediaOptions(mediaOptions)
            .setResumeSavedSession(true)
            .setEnableReconnectionService(true)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}