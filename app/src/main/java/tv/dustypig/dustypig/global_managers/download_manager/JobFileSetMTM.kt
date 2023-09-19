package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Entity
import androidx.room.ForeignKey
import tv.dustypig.dustypig.api.models.MediaTypes


@Entity(
    tableName = "job_fileset_mtm",
    foreignKeys = [
        ForeignKey(
            entity = Job::class,
            parentColumns = arrayOf("mediaId", "mediaType"),
            childColumns = arrayOf("jobMediaId", "jobMediaType"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FileSet::class,
            parentColumns = arrayOf("mediaId"),
            childColumns = arrayOf("fileSetMediaId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["jobMediaId", "jobMediaType", "fileSetMediaId"]
)
data class JobFileSetMTM (
    val jobMediaId: Int,
    val jobMediaType: MediaTypes,
    val fileSetMediaId: Int
)