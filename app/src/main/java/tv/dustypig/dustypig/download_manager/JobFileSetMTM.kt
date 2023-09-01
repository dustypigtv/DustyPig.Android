package tv.dustypig.dustypig.download_manager

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "job_fileset_mtm",
    foreignKeys = [
        ForeignKey(
            entity = Job::class,
            parentColumns = arrayOf("mediaId"),
            childColumns = arrayOf("jobMediaId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FileSet::class,
            parentColumns = arrayOf("mediaId"),
            childColumns = arrayOf("fileSetMediaId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["jobMediaId", "fileSetMediaId"]
)
data class JobFileSetMTM (
    val jobMediaId: Int,
    val fileSetMediaId: Int
)