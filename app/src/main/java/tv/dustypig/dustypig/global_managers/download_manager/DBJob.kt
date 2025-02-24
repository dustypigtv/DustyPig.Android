package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import tv.dustypig.dustypig.api.models.MediaTypes
import java.util.Date

@Entity(
    tableName = "jobs",
    indices = [Index(value = ["mediaId", "mediaType", "profileId"], unique = true)]
)
@TypeConverters(DateConverter::class)
data class DBJob(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mediaId: Int,
    val mediaType: MediaTypes,
    val profileId: Int,
    val title: String,
    val added: Date = Date(),
    var count: Int,
    var pending: Boolean = true,
    val artworkUrl: String? = null,
    val artworkFile: String? = null,
    val artworkIsPoster: Boolean,
    var lastUpdate: Date = Date()
)