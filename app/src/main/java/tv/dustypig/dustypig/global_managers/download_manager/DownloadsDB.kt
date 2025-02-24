package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DBJob::class, DBDownload::class],
    version = 12,
    exportSchema = false
)
abstract class DownloadsDB : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}