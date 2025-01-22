package tv.dustypig.dustypig.api.models

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


data class PlaybackProgress(
    val id: Int,
    val seconds: Double,
    val asOfUTC: String = getTimestamp()
)

private fun getTimestamp(): String {
    val tz = TimeZone.getTimeZone("UTC")
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    sdf.timeZone = tz
    return sdf.format(Calendar.getInstance(tz).time)
}
