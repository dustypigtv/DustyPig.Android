package tv.dustypig.dustypig.download_manager

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
            parentColumns = arrayOf("mediaId"),
            childColumns = arrayOf("mediaId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["fileName"], unique = true)]
)
@TypeConverters(DateConverter::class)
data class Download(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var androidId: Long = 0,
    var url: String,
    var totalBytes: Long = -1,
    var downloadedBytes: Long = 0,
    var fileName: String,
    var complete: Boolean = false,
    var status: DownloadStatus = DownloadStatus.Pending,
    var statusDetails: String = "",
    val mediaId: Int,
    var added: Date = Date(),
    var lastRetry: Date = Date()
)
