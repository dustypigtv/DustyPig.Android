package tv.dustypig.dustypig.global_managers.progress_manager

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progresses",
    indices = [Index(value = ["mediaId", "playlist", "profileId"], unique = true)]
)
data class ProgressEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mediaId: Int,
    val playlist: Boolean,
    val profileId: Int,
    var seconds: Double,
    var timestamp: String = ProgressReportManager.getTimestamp()
)