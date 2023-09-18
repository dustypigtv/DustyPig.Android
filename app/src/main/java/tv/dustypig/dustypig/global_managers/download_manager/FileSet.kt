package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(
    tableName = "fileSets"
)
@TypeConverters(DateConverter::class)
data class FileSet (
    @PrimaryKey val mediaId: Int,
    val title: String,
    var added: Date = Date()
)
