package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(
    tableName = "downloads",
    foreignKeys = [
        ForeignKey(
            entity = FileSet::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("fileSetId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["fileSetId"])]
)
@TypeConverters(DateConverter::class)
data class Download(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var fileSetId: Int,
    var androidId: Long = 0,
    var url: String,
    var totalBytes: Long = -1,
    var downloadedBytes: Long = 0,
    var fileName: String,
    var complete: Boolean = false,
    var status: DownloadStatus = DownloadStatus.Pending,
    var statusDetails: String = "",
    val mediaId: Int,
    val disposition: String,
    var lastRetry: Date = Date(),
    val profileId: Int
)