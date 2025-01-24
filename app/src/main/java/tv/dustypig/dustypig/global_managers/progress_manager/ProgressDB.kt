package tv.dustypig.dustypig.global_managers.progress_manager

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProgressEntity::class], version = 1, exportSchema = false)
abstract class ProgressDB : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
}