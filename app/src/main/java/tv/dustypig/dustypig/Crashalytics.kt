package tv.dustypig.dustypig

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

fun Throwable.logToCrashlytics() {
    Firebase.crashlytics.recordException(this)
}