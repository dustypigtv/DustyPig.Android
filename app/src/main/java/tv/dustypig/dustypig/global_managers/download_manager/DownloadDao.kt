package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import tv.dustypig.dustypig.api.models.MediaTypes


@Dao
interface DownloadDao {

    @Query("SELECT * FROM jobs WHERE profileId = :profileId ORDER BY added")
    suspend fun getJobs(profileId: Int): List<DBJob>

    @Query("SELECT * FROM jobs WHERE mediaId = :mediaId AND mediaType = :mediaType AND profileId = :profileId")
    suspend fun getJob(mediaId: Int, mediaType: MediaTypes, profileId: Int): DBJob?

    @Query("SELECT * FROM downloads WHERE profileId = :profileId ORDER BY sortIndex")
    suspend fun getDownloads(profileId: Int): List<DBDownload>

    @Query("SELECT * FROM downloads WHERE profileId = :profileId AND jobId = :jobId ORDER BY sortIndex")
    suspend fun getDownloads(profileId: Int, jobId: Int): List<DBDownload>

    @Query("SELECT * FROM downloads WHERE profileId = :profileId AND jobId = :jobId AND mediaId = :mediaId")
    suspend fun getDownload(profileId: Int, jobId: Int, mediaId: Int): DBDownload?

    @Insert
    suspend fun insert(job: DBJob)

    @Insert
    suspend fun insert(download: DBDownload)

    @Update
    suspend fun update(job: DBJob)

    @Update
    suspend fun update(download: DBDownload)

    @Delete
    suspend fun delete(job: DBJob)

    @Delete
    suspend fun delete(download: DBDownload)

    @Query("DELETE FROM jobs WHERE profileId = :profileId")
    fun deleteAllJobs(profileId: Int)
}