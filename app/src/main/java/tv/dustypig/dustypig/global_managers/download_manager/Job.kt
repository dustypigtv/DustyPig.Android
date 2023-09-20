package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Entity
import androidx.room.TypeConverters
import tv.dustypig.dustypig.api.models.MediaTypes
import java.util.Date

@Entity(
    tableName = "jobs",
    primaryKeys = ["mediaId", "mediaType"]
)
@TypeConverters(DateConverter::class)
data class Job (
    val mediaId: Int,
    val mediaType: MediaTypes,
    val profileId: Int,
    val title: String,
    val added: Date = Date(),
    var count: Int,
    var pending: Boolean,
    var lastUpdate: Date = Date()
)
