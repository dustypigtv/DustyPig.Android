package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Embedded
import androidx.room.Relation

data class FileSetWithDownloads(
    @Embedded val fileSet: FileSet,
    @Relation(
        parentColumn = "id",
        entityColumn = "fileSetId"
    )
    val downloads: List<Download>
)