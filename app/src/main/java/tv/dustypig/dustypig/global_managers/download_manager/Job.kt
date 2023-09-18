package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import tv.dustypig.dustypig.api.models.MediaTypes
import java.util.Date

@Entity(tableName = "jobs")
@TypeConverters(DateConverter::class)
data class Job (
    @PrimaryKey val mediaId: Int,
    val mediaType: MediaTypes,
    val title: String,
    val added: Date = Date(),
    var count: Int,
    var pending: Boolean,
    var lastUpdate: Date = Date()
)
