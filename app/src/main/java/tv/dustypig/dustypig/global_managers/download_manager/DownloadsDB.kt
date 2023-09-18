package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Job::class, FileSet::class, Download::class, JobFileSetMTM::class], version = 1)
abstract class DownloadsDB : RoomDatabase() {
    abstract fun downloadDao() : DownloadDao
}