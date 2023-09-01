package tv.dustypig.dustypig.download_manager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update


@Dao
interface DownloadDao {

    @Query("SELECT * FROM jobs ORDER BY added")
    suspend fun getJobs() : List<Job>

    @Query("SELECT * FROM jobs WHERE mediaId = :mediaId")
    suspend fun getJob(mediaId: Int) : Job?

    @Query("SELECT * FROM fileSets ORDER BY added")
    suspend fun getFileSets(): List<FileSet>


    @Transaction
    @Query("SELECT * FROM fileSets WHERE mediaId = :mediaId")
    suspend fun getFileSet(mediaId: Int) : FileSetWithDownloads?

    @Query("SELECT * FROM job_fileset_mtm")
    suspend fun getJobFileSetMTMs(): List<JobFileSetMTM>

    @Query("SELECT * FROM job_fileset_mtm WHERE jobMediaId = :jobMediaId")
    suspend fun getJobFileSetMTMs(jobMediaId: Int) : List<JobFileSetMTM>

    @Query("SELECT * FROM job_fileset_mtm WHERE jobMediaId = :jobMediaId AND fileSetMediaId = :fileSetMediaId")
    suspend fun getJobFileSetMTM(jobMediaId: Int, fileSetMediaId: Int) : JobFileSetMTM?


    @Query("SELECT * FROM downloads ORDER BY added")
    suspend fun getDownloads() : List<Download>



    @Insert
    suspend fun insert(job: Job)

    @Insert
    suspend fun insert(fileSet: FileSet)

    @Insert
    suspend fun insert(download: Download)

    @Insert
    suspend fun insert(jobFileSetMTM: JobFileSetMTM)

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

    @Delete
    suspend fun delete(jobFileSetMTM: JobFileSetMTM)
}