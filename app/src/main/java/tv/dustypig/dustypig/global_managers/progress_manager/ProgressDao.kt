package tv.dustypig.dustypig.global_managers.progress_manager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProgressDao {
    @Insert
    suspend fun insert(progress: ProgressEntity)

    @Update
    suspend fun update(progress: ProgressEntity)

    @Delete
    suspend fun delete(progress: ProgressEntity)

    @Query("SELECT * FROM progresses WHERE mediaId = :mediaId AND playlist = :playlist AND profileId = :profileId")
    suspend fun get(mediaId: Int, playlist: Boolean, profileId: Int) : ProgressEntity?

    @Query("SELECT * FROM progresses")
    suspend fun getAll() : List<ProgressEntity>
}