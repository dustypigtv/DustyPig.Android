package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    tableName = "fileSets",
    foreignKeys = [
        ForeignKey(
            entity = Job::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("jobId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["jobId"])]
)
@TypeConverters(DateConverter::class)
data class FileSet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: Int,
    val mediaId: Int,
    val playlistItemId: Int = 0,
    val profileId: Int,
    val title: String,
    var playOrder: Int
)
