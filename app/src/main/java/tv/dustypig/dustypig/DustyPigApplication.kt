package tv.dustypig.dustypig

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import java.lang.ref.WeakReference

@HiltAndroidApp
class DustyPigApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Instance.appContext = WeakReference(this)
    }

    object Instance {
        lateinit var appContext: WeakReference<Context>
            internal set
    }
}