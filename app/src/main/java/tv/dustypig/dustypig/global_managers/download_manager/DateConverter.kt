package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.TypeConverter
import java.util.Date

internal object DateConverter {

    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return dateLong?.let { Date(it) }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
}