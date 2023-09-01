package tv.dustypig.dustypig.download_manager

import androidx.room.Embedded
import androidx.room.Relation

data class FileSetWithDownloads (
    @Embedded val fileSet: FileSet,
    @Relation(
        parentColumn = "mediaId",
        entityColumn = "mediaId"
    )
    val downloads: List<Download>
)