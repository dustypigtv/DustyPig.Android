package tv.dustypig.dustypig.global_managers.cast_manager

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import tv.dustypig.dustypig.R


class CastOptionsProvider : OptionsProvider {

    companion object {
        fun receiverApplicationId(context: Context) = context.getString(R.string.cast_app_id)
    }

    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
            .setReceiverApplicationId(context.getString(R.string.cast_app_id))
            .setResumeSavedSession(true)
            .setEnableReconnectionService(true)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}