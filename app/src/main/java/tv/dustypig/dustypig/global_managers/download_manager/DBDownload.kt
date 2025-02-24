package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import tv.dustypig.dustypig.api.models.MediaTypes

@Entity(
    tableName = "downloads",
    foreignKeys = [
        ForeignKey(
            entity = DBJob::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("jobId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["jobId", "playlistItemId", "mediaId", "mediaType", "profileId"], unique = true)]
)
@TypeConverters(DateConverter::class)
data class DBDownload(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: Int,
    var sortIndex: Int,
    val mediaId: Int,
    val mediaType: MediaTypes,
    val profileId: Int,
    val playlistItemId: Int,
    val title: String,
    val url: String,
    val artworkUrl: String? = null,
    val artworkFile: String? = null,
    val artworkIsPoster: Boolean,
    var played: Double? = null,
    var introStartTime: Double? = null,
    var introEndTime: Double? = null,
    var creditsStartTime: Double? = null
)