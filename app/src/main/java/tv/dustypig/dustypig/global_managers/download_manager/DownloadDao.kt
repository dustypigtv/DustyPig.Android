package tv.dustypig.dustypig.global_managers.download_manager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import tv.dustypig.dustypig.api.models.MediaTypes


@Dao
interface DownloadDao {

    @Query("SELECT * FROM jobs WHERE profileId = :profileId ORDER BY added")
    suspend fun getJobs(profileId: Int) : List<Job>

    @Query("SELECT * FROM jobs WHERE mediaId = :mediaId AND mediaType = :mediaType AND profileId = :profileId")
    suspend fun getJob(mediaId: Int, mediaType: MediaTypes, profileId: Int) : Job?

    @Query("SELECT * FROM fileSets WHERE jobId = :jobId")
    suspend fun getFileSets(jobId: Int): List<FileSet>

    @Query("SELECT * FROM fileSets WHERE profileId = :profileId ORDER BY jobId, playOrder")
    suspend fun getAllFileSets(profileId: Int): List<FileSet>


    @Transaction
    @Query("SELECT * FROM fileSets WHERE jobId = :jobId AND mediaId = :mediaId")
    suspend fun getFileSetAndDownloadsByMediaId(jobId: Int, mediaId: Int): FileSetWithDownloads?

    @Transaction
    @Query("SELECT * FROM fileSets WHERE jobId = :jobId AND playlistItemId = :playlistItemId")
    suspend fun getFileSetAndDownloadsByPlaylistItemId(jobId: Int, playlistItemId: Int): FileSetWithDownloads?


    @Query("SELECT * FROM downloads WHERE profileId = :profileId ")
    suspend fun getDownloads(profileId: Int) : List<Download>



    @Insert
    suspend fun insert(job: Job)

    @Insert
    suspend fun insert(fileSet: FileSet)

    @Insert
    suspend fun insert(download: Download)

    @Update
    suspend fun update(job: Job)

    @Update
    suspend fun update(fileSet: FileSet)

    @Update
    suspend fun update(download: Download)

    @Delete
    suspend fun delete(job: Job)

    @Delete
    suspend fun delete(fileSet: FileSet)

    @Delete
    suspend fun delete(download: Download)

    @Query("DELETE FROM jobs WHERE profileId = :profileId")
    fun deleteAllJobs(profileId: Int)
}