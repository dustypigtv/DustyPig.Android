package tv.dustypig.dustypig.download_manager

import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.room.Room
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tv.dustypig.dustypig.DustyPigApplication
import tv.dustypig.dustypig.SettingsManager
import tv.dustypig.dustypig.api.API
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.ExternalSubtitle
import tv.dustypig.dustypig.api.models.MediaTypes
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.Timer
import kotlin.concurrent.schedule
import android.app.DownloadManager as AndroidDownloadManager

@OptIn(DelicateCoroutinesApi::class)
object DownloadManager {

    private const val TAG = "DownloadManager"

    private const val UPDATE_MINUTES = 5

    private val _androidDownloadManager = getContext().getSystemService(Context.DOWNLOAD_SERVICE) as AndroidDownloadManager

    private val _db = Room.databaseBuilder(
        context = getContext(),
        klass = DownloadsDB::class.java,
        name = "downloads.db"
    )
        .fallbackToDestructiveMigration()
        .build()
        .downloadDao()


    private val _statusTimer = Timer()
    private var _statusTimerBusy = false

    private val _updateTimer = Timer()
    private var _updateTimerBusy = false

    private val _downloadFlow = MutableSharedFlow<List<UIJob>>(replay = 1)
    val downloads = _downloadFlow.asSharedFlow()


    init {

        _statusTimer.schedule(
            delay = 0,
            period = 100
        ) {
            statusTimerTick()
        }

        _updateTimer.schedule(
            delay = 1000,
            period = 1000
        ) {
            updateTimerTick()
        }
    }


    fun start() {
        Log.d(TAG, "Started")
    }

    private fun getContext() = DustyPigApplication.Instance.appContext.get()!!

    private suspend fun rootDir(): File {
        val ret = if (SettingsManager.loadStoreDownloadsExternally().first())
            getContext().getExternalFilesDir(null)!!
        else
            getContext().filesDir!!

        withContext(Dispatchers.IO) {
            File(ret, ".nomedia").createNewFile()
        }

        return ret
    }

    private fun getLong(cursor: Cursor, column: String): Long {
        val index = cursor.getColumnIndex(column)
        return cursor.getLong(index)
    }

    private fun getInt(cursor: Cursor, column: String): Int {
        val index = cursor.getColumnIndex(column)
        return cursor.getInt(index)
    }

    private fun Date.secondsSince(): Long {
        val diff = Date().time - this.time
        return diff / 1000
    }

    private fun Date.minutesSince(): Long {
        return this.secondsSince() / 60
    }

    private fun statusTimerTick() {

        if(_statusTimerBusy)
            return
        _statusTimerBusy = true
        GlobalScope.launch {
            try {
                statusTimerWork()
            } catch (ex: Exception) {
                Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
            }
            _statusTimerBusy = false
        }
    }

    /**
     * This syncs the db with the os downloader
     */
    private suspend fun statusTimerWork() {

        //Get active downloads
        val downloads = _db.getDownloads()
        val cursor = _androidDownloadManager.query(AndroidDownloadManager.Query())
        while (cursor.moveToNext()) {

            val androidId = getLong(cursor, AndroidDownloadManager.COLUMN_ID)
            val download = downloads.firstOrNull { it.androidId == androidId }
            if (download == null) {
                //No longer valid: Remove
                _androidDownloadManager.remove(androidId)
            } else {

                val totalBytes = getLong(cursor, AndroidDownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                if (totalBytes > 0) {
                    download.totalBytes = totalBytes
                    download.downloadedBytes = getLong(cursor, AndroidDownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    _db.update(download)
                }

                val statusCode = getInt(cursor, AndroidDownloadManager.COLUMN_STATUS)
                val statusReasonCode = getInt(cursor, AndroidDownloadManager.COLUMN_REASON)
                if (statusCode == AndroidDownloadManager.STATUS_SUCCESSFUL) {

                    //Move tmp file to final destination, remove from manager
                    try {
                        val tmpFile = File(rootDir().absolutePath + "/${download.fileName}.tmp")
                        val finFile = File(rootDir().absolutePath + "/${download.fileName}")
                        tmpFile.renameTo(finFile)
                        download.complete = true
                        _db.update(download)
                        _androidDownloadManager.remove(androidId)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }

                } else if (statusCode == AndroidDownloadManager.STATUS_FAILED) {

                    val statusDetails = when (statusReasonCode) {
                        AndroidDownloadManager.ERROR_DEVICE_NOT_FOUND -> "External Storage Missing"
                        AndroidDownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File Already Exists"
                        AndroidDownloadManager.ERROR_HTTP_DATA_ERROR -> "Data Error"
                        AndroidDownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "HTTP Error"
                        AndroidDownloadManager.ERROR_TOO_MANY_REDIRECTS -> "HTTP Error"
                        AndroidDownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient Space"
                        else -> "Unknown Error"
                    }

                    if (download.status != DownloadStatus.Error || download.statusDetails != statusDetails) {
                        download.status = DownloadStatus.Error
                        download.statusDetails = statusDetails
                        _db.update(download)
                    }

                    //Retry once every 10 seconds
                    if (download.lastRetry.secondsSince() >= 10) {
                        _androidDownloadManager.remove(androidId)
                        download.androidId = 0
                        download.lastRetry = Date()
                        _db.update(download)
                    }

                } else {

                    val status = when (statusCode) {
                        AndroidDownloadManager.STATUS_RUNNING -> DownloadStatus.Running
                        AndroidDownloadManager.STATUS_PAUSED -> DownloadStatus.Paused
                        else -> DownloadStatus.Pending
                    }

                    if (download.status != status) {
                        download.status = status
                        _db.update(download)
                    }

                    if (status == DownloadStatus.Paused) {
                        val reason = when (statusReasonCode) {
                            AndroidDownloadManager.PAUSED_QUEUED_FOR_WIFI -> "Waiting for Wi-Fi"
                            AndroidDownloadManager.PAUSED_WAITING_FOR_NETWORK -> "No Internet Connection"
                            AndroidDownloadManager.PAUSED_WAITING_TO_RETRY -> "Waiting to Retry"
                            else -> "Paused Temporarily"
                        }

                        if (download.statusDetails != reason) {
                            download.statusDetails = reason
                            _db.update(download)
                        }
                    }
                }
            }
        }
        cursor.close()

        //Remove any files that are invalid
        val jobs = _db.getJobs()
        for (file in rootDir().listFiles()!!) {

            var valid = file.name == ".nomedia"

            //Jobs store info in json files
            if(!valid) {
                for (j in jobs) {
                    if (file.name == "${j.mediaId}.json") {
                        valid = true
                        break
                    }
                }
            }

            //Downloads have unique filenames
            if (!valid) {
                for (d in downloads) {
                    if (file.name == d.fileName || file.name == "${d.fileName}.tmp") {
                        valid = true
                        break
                    }
                }
            }

            if(!valid)
                file.delete()
        }


        //Add any downloads that are not started
        for(download in downloads.filter{ it.androidId == 0L }) {
            var file = File(rootDir().absolutePath + "/${download.fileName}")
            if (file.exists())
                file.delete()
            file = File(rootDir(), "${download.fileName}.tmp")
            if (file.exists())
                file.delete()
            val url = android.net.Uri.parse(download.url)
            val request = android.app.DownloadManager.Request(url)
            request.setDestinationUri(android.net.Uri.fromFile(file))
            request.setAllowedOverMetered(SettingsManager.loadDownloadOverCellular().first())
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_HIDDEN)
            download.androidId = _androidDownloadManager.enqueue(request)
            _db.update(download)
        }


        //Update the flow
        val jobFileSetMTMS = _db.getJobFileSetMTMs()
        val fileSetWithDownloadsList = _db.getFileSetsAndDownloads()
        val uiJobs = ArrayList<UIJob>()
        for(job in jobs) {
            val uiJob = UIJob(
                mediaId = job.mediaId,
                mediaType = job.mediaType,
                title = job.title,
                percent = 0.0f,
                status = if(job.pending) DownloadStatus.Pending else DownloadStatus.Finished,
                statusDetails = "",
                downloads = listOf()
            )
            uiJobs.add(uiJob)

            var jobTotalSize = 0L
            var jobCompleted = 0L
            val uiDownloads = ArrayList<UIDownload>()

            for(jobFileSetMTM in jobFileSetMTMS.filter { it.jobMediaId == job.mediaId }) {
                val fileSetWithDownloads = fileSetWithDownloadsList.first{ it.fileSet.mediaId == jobFileSetMTM.fileSetMediaId }
                var fileSetTotalSize = 0L
                var fileSetCompleted = 0L
                var fileSetStatus = DownloadStatus.Finished
                var fileSetStatusDetails = ""
                for(download in fileSetWithDownloads.downloads) {

                    if(download.totalBytes < 0) {
                        uiJob.status = DownloadStatus.Pending
                    } else {
                        fileSetTotalSize += download.totalBytes.coerceAtLeast(0L)
                        fileSetCompleted += download.downloadedBytes
                        if (!download.complete) {
                            if (fileSetStatus == DownloadStatus.Finished) {
                                fileSetStatus = download.status
                                fileSetStatusDetails = download.statusDetails

                                if (uiJob.status == DownloadStatus.Finished || uiJob.status == DownloadStatus.Running) {
                                    uiJob.status = download.status
                                    uiJob.statusDetails = download.statusDetails
                                }
                            }
                        }
                    }

                }

                uiDownloads.add(
                    UIDownload(
                        title = fileSetWithDownloads.fileSet.title,
                        percent = if(fileSetTotalSize > 0) fileSetCompleted.toFloat() / fileSetTotalSize.toFloat() else 0.0f,
                        status = fileSetStatus,
                        statusDetails = fileSetStatusDetails
                ))

                jobTotalSize += fileSetTotalSize.coerceAtLeast(0L)
                jobCompleted += fileSetCompleted
            }

            uiJob.percent = if(jobTotalSize > 0) jobCompleted.toFloat() / jobTotalSize.toFloat() else 0.0f
            uiJob.downloads = uiDownloads.toList()
        }

        _downloadFlow.tryEmit(uiJobs.toList())

    }

    private fun updateTimerTick(){
        if(_updateTimerBusy)
            return
        _updateTimerBusy = true
        GlobalScope.launch {
            try {
                updateTimerWork()
            } catch (ex: Exception) {
                Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
            }
            _updateTimerBusy = false
        }
    }

    private suspend fun updateTimerWork() {

        val jobs = _db.getJobs()
        for(job in jobs) {
            try {
                var update = job.pending
                if(!update && (job.mediaType == MediaTypes.Series || job.mediaType == MediaTypes.Playlist)){
                    update = job.lastUpdate.minutesSince() >= UPDATE_MINUTES
                }

                if (update) {
                    when (job.mediaType) {
                        MediaTypes.Movie -> updateMovie(job)
                        MediaTypes.Series -> updateSeries(job)
                        MediaTypes.Playlist -> updatePlaylist(job)
                        MediaTypes.Episode -> updateEpisode(job)
                    }

                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        //Cleanup orphaned downloads
        val fileSets = _db.getFileSets()
        val jobFileSetMTMs = _db.getJobFileSetMTMs()

        for(fileSet in fileSets) {
            val valid = jobFileSetMTMs.any {
                it.fileSetMediaId == fileSet.mediaId
            }
            if(!valid)
                _db.delete(fileSet)
        }
    }

    private suspend fun saveFile(fileName: String, data: Any) {
        val file = File(rootDir(), fileName)
        if(file.exists())
            file.delete()
        file.writeText(Gson().toJson(data))
    }

    private suspend fun addOrUpdateDownload(fileSetWithDownloads: FileSetWithDownloads, url: String?, disposition: String) {

        if(url.isNullOrBlank())
            return

        var ext = url.split("?")[0]
        ext = ext.substring(ext.lastIndexOf("."))
        if(ext.isBlank())
            ext = ".dat"


        val fileName = "${fileSetWithDownloads.fileSet.mediaId}.$disposition$ext"

        var dl = fileSetWithDownloads.downloads.firstOrNull {
            it.fileName == fileName
        }

        if(dl == null) {

            dl = Download(
                url = url,
                totalBytes = -1, //getSize(url),
                fileName = fileName,
                mediaId = fileSetWithDownloads.fileSet.mediaId,
                status = DownloadStatus.Pending,
                statusDetails = ""
            )

            _db.insert(download = dl)

        } else {

            if (dl.fileName != fileName || dl.url != url) {

                dl.url = url
                dl.fileName = fileName
                dl.complete = false
                dl.totalBytes = -1 //getSize(url)
                dl.complete = false
                dl.androidId = 0
                dl.downloadedBytes = 0
                dl.status = DownloadStatus.Pending
                dl.statusDetails = ""

                _db.update(download = dl)

            }

        }
    }

    private suspend fun addOrUpdateSubtitleDownload(fileSetWithDownloads: FileSetWithDownloads, sub: ExternalSubtitle) {

        if(sub.url.isBlank())
            return

        var ext = sub.url.split("?")[0]
        ext = ext.substring(startIndex = ext.lastIndexOf(string = "."))
        if(ext.isBlank())
            ext = ".dat"

        val fileName = "${fileSetWithDownloads.fileSet.mediaId}.subtitle.${sub.name}$ext"

        var dl = fileSetWithDownloads.downloads.firstOrNull {
            it.fileName == fileName
        }
        if(dl == null) {
            dl = Download(
                url = sub.url,
                totalBytes = -1, //getSize(sub.url),
                fileName = fileName,
                mediaId = fileSetWithDownloads.fileSet.mediaId,
                status = DownloadStatus.Pending,
                statusDetails = ""
            )
            _db.insert(download = dl)
        } else {
            if (dl.fileName != fileName || dl.url != sub.url) {
                dl.url = sub.url
                dl.fileName = fileName
                dl.complete = false
                dl.totalBytes = -1 //getSize(sub.url)
                dl.complete = false
                dl.androidId = 0
                dl.downloadedBytes = 0
                dl.status = DownloadStatus.Pending
                dl.statusDetails = ""
                _db.update(download = dl)
            }
        }

    }

    private suspend fun updateMovie(job: Job) {

        val detailedMovie = API.Movies.movieDetails(id = job.mediaId)
        saveFile(fileName = "${detailedMovie.id}.json", data = detailedMovie)

        var fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)
        if(fileSetWithDownloads == null) {
            _db.insert(
                FileSet(
                mediaId = job.mediaId,
                title = detailedMovie.displayTitle()
            ))
            fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)!!
        }

        if(_db.getJobFileSetMTM(job.mediaId, fileSetWithDownloads.fileSet.mediaId) == null) {
            _db.insert(
                jobFileSetMTM = JobFileSetMTM(
                    jobMediaId = job.mediaId,
                    fileSetMediaId = job.mediaId
                )
            )
        }


        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedMovie.artworkUrl, disposition = "poster")
        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedMovie.backdropUrl, disposition = "backdrop")
        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedMovie.bifUrl, disposition = "bif")

        if(detailedMovie.externalSubtitles?.isNotEmpty() == true) {
            for(sub in detailedMovie.externalSubtitles) {
                addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSetWithDownloads, sub = sub)
            }
        }

        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedMovie.videoUrl, disposition = "video")

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }

    private suspend fun updateSeries(job: Job) {

        val detailedSeries = API.Series.seriesDetails(job.mediaId)
        saveFile(fileName = "${detailedSeries.id}.json", data = detailedSeries)

        var fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)
        if(fileSetWithDownloads == null) {
            _db.insert(
                FileSet(
                    mediaId = job.mediaId,
                    title = detailedSeries.title
                ))
            fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)!!
        }

        //Used for tracking orphaned downloads
        if(_db.getJobFileSetMTM(job.mediaId, fileSetWithDownloads.fileSet.mediaId) == null) {
            _db.insert(
                jobFileSetMTM = JobFileSetMTM(
                    jobMediaId = job.mediaId,
                    fileSetMediaId = job.mediaId
                )
            )
        }

        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedSeries.artworkUrl, disposition = "poster")
        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedSeries.backdropUrl, disposition = "backdrop")


        //Identify ids of episodes that should be downloaded
        var upNext = detailedSeries.episodes?.firstOrNull {
            it.upNext
        }
        if(upNext == null)
            upNext = detailedSeries.episodes?.first()

        var reachedUpNext = false
        var count = 0
        val itemIds = ArrayList<Int>()
        if(upNext != null) {
            for (episode in detailedSeries.episodes!!) {
                if (upNext.id == episode.id)
                    reachedUpNext = true
                if (reachedUpNext) {
                    itemIds.add(episode.id)
                    count++
                    if (count >= job.count)
                        break
                }
            }
        }

        //Remove any episodes that should no longer be downloaded
        val jobMTMs = _db.getJobFileSetMTMs(jobMediaId = job.mediaId)
        for(jobMTM in jobMTMs) {
            if (!itemIds.any {
                    it == jobMTM.fileSetMediaId
                }) {
                if(jobMTM.fileSetMediaId != job.mediaId) {
                    _db.delete(jobMTM)
                }
            }
        }


        //Add any episodes that should be downloaded
        if(upNext != null) {
            for (mediaId in itemIds) {
                val episode = detailedSeries.episodes!!.first {
                    it.id == mediaId
                }
                fileSetWithDownloads = _db.getFileSet(mediaId = episode.id)
                if (fileSetWithDownloads == null) {
                    _db.insert(
                        FileSet(
                            mediaId = episode.id,
                            title = episode.fullDisplayTitle()
                        )
                    )
                    fileSetWithDownloads = _db.getFileSet(mediaId = episode.id)!!
                }

                //Used for tracking orphaned downloads
                if (_db.getJobFileSetMTM(job.mediaId, episode.id) == null) {
                    _db.insert(
                        jobFileSetMTM = JobFileSetMTM(
                            jobMediaId = job.mediaId,
                            fileSetMediaId = episode.id
                        )
                    )
                }

                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = episode.artworkUrl, disposition = "screenshot")
                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = episode.bifUrl, disposition = "bif")

                if (episode.externalSubtitles != null) {
                    for (sub in episode.externalSubtitles)
                        addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSetWithDownloads, sub = sub)
                }

                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = episode.videoUrl, disposition = "video")

            }
        }

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }

    private suspend fun updateEpisode(job: Job) {

        val detailedEpisode = API.Episodes.details(id = job.mediaId)
        saveFile(fileName = "${detailedEpisode.id}.json", data = detailedEpisode)

        var fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)
        if(fileSetWithDownloads == null) {
            _db.insert(
                FileSet(
                    mediaId = job.mediaId,
                    title = detailedEpisode.fullDisplayTitle()
                ))
            fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)!!
        }

        if(_db.getJobFileSetMTM(job.mediaId, fileSetWithDownloads.fileSet.mediaId) == null) {
            _db.insert(
                jobFileSetMTM = JobFileSetMTM(
                    jobMediaId = job.mediaId,
                    fileSetMediaId = job.mediaId
                )
            )
        }


        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedEpisode.artworkUrl, disposition = "poster")
        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedEpisode.bifUrl, disposition = "bif")

        if(detailedEpisode.externalSubtitles?.isNotEmpty() == true) {
            for(sub in detailedEpisode.externalSubtitles) {
                addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSetWithDownloads, sub = sub)
            }
        }

        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedEpisode.videoUrl, disposition = "video")

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)

    }

    private suspend fun updatePlaylist(job: Job) {

        val detailedPlaylist = API.Playlists.playlistDetails(job.mediaId)
        saveFile(fileName = "${detailedPlaylist.id}.json", data = detailedPlaylist)

        var fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)
        if(fileSetWithDownloads == null) {
            _db.insert(
                FileSet(
                    mediaId = job.mediaId,
                    title = detailedPlaylist.name
                ))
            fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)!!
        }

        //Used for tracking orphaned downloads
        if(_db.getJobFileSetMTM(job.mediaId, fileSetWithDownloads.fileSet.mediaId) == null) {
            _db.insert(
                jobFileSetMTM = JobFileSetMTM(
                    jobMediaId = job.mediaId,
                    fileSetMediaId = job.mediaId
                )
            )
        }

        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedPlaylist.artworkUrl, disposition = "poster")


        //Identify ids of items that should be downloaded
        var upNext = detailedPlaylist.items?.firstOrNull {
            it.index == detailedPlaylist.currentIndex
        }
        if(upNext == null)
            upNext = detailedPlaylist.items?.first()

        var reachedUpNext = false
        var count = 0
        val itemIds = ArrayList<Int>()
        if(upNext != null) {
            for (episode in detailedPlaylist.items!!) {
                if (upNext.id == episode.id)
                    reachedUpNext = true
                if (reachedUpNext) {
                    itemIds.add(episode.id)
                    count++
                    if (count >= job.count)
                        break
                }
            }
        }

        //Remove any items that should no longer be downloaded
        val jobMTMs = _db.getJobFileSetMTMs(jobMediaId = job.mediaId)
        for(jobMTM in jobMTMs) {
            if (!itemIds.any {
                    it == jobMTM.fileSetMediaId
                }) {
                if(jobMTM.fileSetMediaId != job.mediaId) {
                    _db.delete(jobMTM)
                }
            }
        }



        //Add any items that should be downloaded
        if(upNext != null) {
            for (mediaId in itemIds) {
                val playlistItem = detailedPlaylist.items!!.first {
                    it.id == mediaId
                }
                fileSetWithDownloads = _db.getFileSet(mediaId = playlistItem.id)
                if (fileSetWithDownloads == null) {
                    _db.insert(
                        FileSet(
                            mediaId = playlistItem.id,
                            title = playlistItem.title
                        )
                    )
                    fileSetWithDownloads = _db.getFileSet(mediaId = playlistItem.id)!!
                }

                //Used for tracking orphaned downloads
                if (_db.getJobFileSetMTM(job.mediaId, playlistItem.id) == null) {
                    _db.insert(
                        jobFileSetMTM = JobFileSetMTM(
                            jobMediaId = job.mediaId,
                            fileSetMediaId = playlistItem.id
                        )
                    )
                }

                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = playlistItem.artworkUrl, disposition = "screenshot")
                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = playlistItem.bifUrl, disposition = "bif")

                if (playlistItem.externalSubtitles != null) {
                    for (sub in playlistItem.externalSubtitles)
                        addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSetWithDownloads, sub = sub)
                }

                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = playlistItem.videoUrl, disposition = "video")

            }
        }

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }



    suspend fun addMovie(detailedMovie: DetailedMovie) {

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        _db.insert(
            Job(
                mediaId = detailedMovie.id,
                mediaType = MediaTypes.Movie,
                title = detailedMovie.displayTitle(),
                count = 1,
                pending = true,
                lastUpdate = lastUpdate.time
            )
        )
    }

    suspend fun addOrUpdateSeries(detailedSeries: DetailedSeries, count: Int) {

        if(count == 0) {
            delete(detailedSeries.id)
            return
        }

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = _db.getJob(detailedSeries.id)
        if(job == null) {
            _db.insert(
                Job(
                    mediaId = detailedSeries.id,
                    mediaType = MediaTypes.Series,
                    title = detailedSeries.title,
                    count = count,
                    pending = true,
                    lastUpdate = lastUpdate.time
                )
            )
        } else if(job.count != count) {
            job.pending = true
            job.count = count
            job.lastUpdate = lastUpdate.time
            _db.update(job)
        }

    }

    suspend fun addEpisode(detailedEpisode: DetailedEpisode) {

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        _db.insert(
            Job(
                mediaId = detailedEpisode.id,
                mediaType = MediaTypes.Episode,
                title = "S${detailedEpisode.seasonNumber}:${detailedEpisode.episodeNumber}: ${detailedEpisode.title}",
                count = 1,
                pending = true,
                lastUpdate = lastUpdate.time
            )
        )
    }

    suspend fun addOrUpdatePlaylist(detailedPlaylist: DetailedPlaylist, count: Int) {

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = _db.getJob(detailedPlaylist.id)
        if(job == null) {
            _db.insert(
                Job(
                    mediaId = detailedPlaylist.id,
                    mediaType = MediaTypes.Playlist,
                    title = detailedPlaylist.name,
                    count = count,
                    pending = true,
                    lastUpdate = lastUpdate.time
                )
            )
        } else if(job.count != count) {
            job.pending = true
            job.count = count
            job.lastUpdate = lastUpdate.time
            _db.update(job)
        }

    }



    suspend fun delete(id: Int) {
        val job = _db.getJob(id)
        if (job != null)
            _db.delete(job)
    }

    suspend fun getJobCount(id: Int): Int {
        return _db.getJob(id)?.count ?: 0
    }

}