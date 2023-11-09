package tv.dustypig.dustypig.global_managers.download_manager

import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.os.StatFs
import android.util.Log
import androidx.room.Room
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.ExternalSubtitle
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.repositories.EpisodesRepository
import tv.dustypig.dustypig.api.repositories.MoviesRepository
import tv.dustypig.dustypig.api.repositories.PlaylistRepository
import tv.dustypig.dustypig.api.repositories.SeriesRepository
import tv.dustypig.dustypig.global_managers.NetworkManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics
import java.io.File
import java.io.IOException
import java.util.Calendar
import java.util.Date
import java.util.Timer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.schedule
import android.app.DownloadManager as AndroidDownloadManager


@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager,
    private val networkManager: NetworkManager,
    private val moviesRepository: MoviesRepository,
    private val seriesRepository: SeriesRepository,
    private val episodesRepository: EpisodesRepository,
    private val playlistRepository: PlaylistRepository
) {

    companion object {
        private const val TAG = "DownloadManager"
        private const val UPDATE_MINUTES = 5
        private const val DISPOSITION_VIDEO = "video"
        private const val DISPOSITION_POSTER = "poster"
        private const val DISPOSITION_SCREENSHOT = "screenshot"
        private const val DISPOSITION_BACKDROP = "backdrop"
        private const val DISPOSITION_BIF = "bif"
        private const val DISPOSITION_SUBTITLE = "subtitle"
        private const val NO_MEDIA = ".nomedia"
    }

    private val _androidDownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as AndroidDownloadManager
    private val _okHttpClient = OkHttpClient()
    private val _rootDir = File(context.getExternalFilesDir(null)!!, "downloads")
    private var _profileId = 0
    private var _downloadOverMobile = false

    private val _statusTimer = Timer()
    private var _statusTimerBusy = false

    private val _updateTimer = Timer()
    private var _updateTimerBusy = false

    private val _downloadFlow = MutableSharedFlow<List<UIJob>>(replay = 1)
    val downloads = _downloadFlow.asSharedFlow()

    private val _db = Room.databaseBuilder(
        context = context,
        klass = DownloadsDB::class.java,
        name = "downloads.db"
    )
        .fallbackToDestructiveMigration()
        .build()
        .downloadDao()


    private val displayMetrics = Resources.getSystem().displayMetrics
    private val urlParameters = "displayWidth=${displayMetrics.widthPixels}&displayHeight=${displayMetrics.heightPixels}"


    init {

        _rootDir.mkdirs()
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                File(_rootDir, NO_MEDIA).createNewFile()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            settingsManager.profileIdFlow.collectLatest { _profileId = it }
        }

        CoroutineScope(Dispatchers.IO).launch {
            settingsManager.downloadOverMobileFlow.collectLatest { _downloadOverMobile = it }
        }

        _statusTimer.schedule(
            delay = 1000,
            period = 250
        ) {
            statusTimerTick()
        }

        _updateTimer.schedule(
            delay = 10000,
            period = 10000
        ) {
            updateTimerTick()
        }
    }


    fun start() {
        Log.d(TAG, "Started")
    }

    private fun MediaTypes.asString(): String = when(this) {
        MediaTypes.Movie -> "movie"
        MediaTypes.Series -> "series"
        MediaTypes.Episode -> "episode"
        MediaTypes.Playlist -> "playlist"
    }

    private fun getSize(url: String): Long {
        val request = Request.Builder()
            .url(url)
            .head()
            .build()

        return _okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful)
                throw IOException("Unexpected code $response")
            response.header("Content-Length")?.toLong() ?: -1
        }
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                statusTimerWork()
            } catch (ex: Exception) {
                Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
                ex.logToCrashlytics()
            }
            _statusTimerBusy = false
        }
    }

    /**
     * This syncs the db with the os downloader
     */
    private suspend fun statusTimerWork() {

        //Don't do anything until we know what to do
        if(_profileId == 0)
            return

        if(!networkManager.isConnected())
            return

        var downloads = _db.getDownloads()
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
                        val tmpFile = File(_rootDir.absolutePath + "/${download.fileName}.tmp")
                        val finFile = File(_rootDir.absolutePath + "/${download.fileName}")
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

                    if(statusReasonCode == AndroidDownloadManager.ERROR_INSUFFICIENT_SPACE && download.totalBytes > 0) {
                        //Retry when enough space available
                        val stat = StatFs(_rootDir.path)
                        if (download.totalBytes < stat.availableBytes) {
                            _androidDownloadManager.remove(androidId)
                            download.androidId = 0
                            download.lastRetry = Date()
                            _db.update(download)
                        }
                    } else {
                        //Retry once every 10 seconds
                        if (download.lastRetry.secondsSince() >= 10) {
                            _androidDownloadManager.remove(androidId)
                            download.androidId = 0
                            download.lastRetry = Date()
                            _db.update(download)
                        }
                    }

                } else {

                    var status = when (statusCode) {
                        AndroidDownloadManager.STATUS_RUNNING -> DownloadStatus.Running
                        AndroidDownloadManager.STATUS_PAUSED -> DownloadStatus.Paused
                        else -> DownloadStatus.Pending
                    }

                    if(status == DownloadStatus.Paused && statusReasonCode == AndroidDownloadManager.PAUSED_WAITING_TO_RETRY) {
                        status = DownloadStatus.Running
                    }

                    if (download.status != status) {
                        download.status = status
                        _db.update(download)
                    }

                    if (status == DownloadStatus.Paused) {
                        val reason = when (statusReasonCode) {
                            AndroidDownloadManager.PAUSED_QUEUED_FOR_WIFI -> "Waiting for Wi-Fi"
                            AndroidDownloadManager.PAUSED_WAITING_FOR_NETWORK -> "No Internet Connection"
                            //AndroidDownloadManager.PAUSED_WAITING_TO_RETRY -> "Waiting to Retry"
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
        val jobs = _db.getJobs(_profileId)
        for (file in _rootDir.listFiles()!!) {

            var valid = file.name == NO_MEDIA

            //Jobs store info in json files
            if(!valid) {
                for (j in jobs) {
                    if (file.name == "${j.mediaId}.${j.mediaType.asString()}.json") {
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

            if (!valid)
                file.delete()
        }


        //Add any support files that are not started (max of 3)
        val runningSupportFiles = downloads.filter {
            it.androidId != 0L && it.disposition != DISPOSITION_VIDEO && !it.complete
        }
        if(runningSupportFiles.count() < 3) {
            val nextDownload = downloads.firstOrNull { it.androidId == 0L && it.disposition != DISPOSITION_VIDEO }
            if(nextDownload != null) {
                var file = File(_rootDir.absolutePath + "/${nextDownload.fileName}")
                if (file.exists())
                    file.delete()
                file = File(_rootDir, "${nextDownload.fileName}.tmp")
                if (file.exists())
                    file.delete()

                var url = nextDownload.url
                if(listOf(DISPOSITION_POSTER, DISPOSITION_BACKDROP, DISPOSITION_SCREENSHOT).contains(nextDownload.disposition)) {
                    url += if (url.contains("?")) "&$urlParameters" else "?$urlParameters"
                }

                val uri = android.net.Uri.parse(url)
                val request = android.app.DownloadManager.Request(uri)
                request.setDestinationUri(android.net.Uri.fromFile(file))
                request.setAllowedOverMetered(_downloadOverMobile)
                request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_HIDDEN)
                nextDownload.androidId = _androidDownloadManager.enqueue(request)
                _db.update(nextDownload)
            }
        }

        //Only download 1 video at a time
        val runningVideos = downloads.filter {
            it.androidId != 0L && it.disposition == DISPOSITION_VIDEO && !it.complete
        }
        if(runningVideos.isEmpty()) {
            val nextDownload = downloads.firstOrNull { it.androidId == 0L && it.disposition == DISPOSITION_VIDEO }
            if (nextDownload != null) {
                var file = File(_rootDir.absolutePath + "/${nextDownload.fileName}")
                if (file.exists())
                    file.delete()
                file = File(_rootDir, "${nextDownload.fileName}.tmp")
                if (file.exists())
                    file.delete()

                var url = nextDownload.url
                url += if(url.contains("?")) "&$urlParameters" else "?$urlParameters"

                val uri = android.net.Uri.parse(url)
                val request = android.app.DownloadManager.Request(uri)
                request.setDestinationUri(android.net.Uri.fromFile(file))
                request.setAllowedOverMetered(_downloadOverMobile)
                request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_HIDDEN)
                nextDownload.androidId = _androidDownloadManager.enqueue(request)
                _db.update(nextDownload)
            }
        }


        //Update the flow
        downloads = _db.getDownloads()
        val jobFileSetMTMS = _db.getJobFileSetMTMs()
        val fileSetWithDownloadsList = _db.getFileSetsAndDownloads()
        val uiJobs = ArrayList<UIJob>()
        for(job in jobs) {

            var artworkPoster = false
            var artDL = downloads.firstOrNull { it.mediaId == job.mediaId && it.disposition == DISPOSITION_SCREENSHOT }
            if(artDL == null)
                artDL = downloads.firstOrNull { it.mediaId == job.mediaId && it.disposition == DISPOSITION_BACKDROP }
            if(artDL == null) {
                artDL = downloads.firstOrNull { it.mediaId == job.mediaId && it.disposition == DISPOSITION_POSTER }
                artworkPoster = artDL != null
            }

            var artFile = _rootDir.listFiles()?.firstOrNull { it.name == artDL?.fileName }?.path
            if(artFile == null) {
                artFile = artDL?.url
                artworkPoster = artDL?.disposition == DISPOSITION_POSTER
            }

            val uiJob = UIJob(
                mediaId = job.mediaId,
                mediaType = job.mediaType,
                title = job.title,
                artworkUrl = artFile ?: "",
                artworkPoster = artworkPoster,
                percent = 0.0f,
                status = if(job.pending) DownloadStatus.Pending else DownloadStatus.Finished,
                statusDetails = "",
                downloads = listOf(),
                count = job.count
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
                        fileSetTotalSize += download.totalBytes
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

                artworkPoster = false
                artDL = downloads.firstOrNull { it.mediaId == jobFileSetMTM.fileSetMediaId && it.disposition == DISPOSITION_SCREENSHOT }
                if(artDL == null)
                    artDL = downloads.firstOrNull { it.mediaId == jobFileSetMTM.fileSetMediaId && it.disposition == DISPOSITION_BACKDROP }
                if(artDL == null) {
                    artDL = downloads.firstOrNull { it.mediaId == jobFileSetMTM.fileSetMediaId && it.disposition == DISPOSITION_POSTER }
                    artworkPoster = artDL != null
                }

                artFile = _rootDir.listFiles()?.firstOrNull { it.name == artDL?.fileName }?.path
                if(artFile == null) {
                    artFile = artDL?.url
                    artworkPoster = artDL?.disposition == DISPOSITION_POSTER
                }

                uiDownloads.add(
                    UIDownload(
                        title = fileSetWithDownloads.fileSet.title,
                        percent = if(fileSetTotalSize > 0) fileSetCompleted.toFloat() / fileSetTotalSize.toFloat() else 0.0f,
                        status = fileSetStatus,
                        statusDetails = fileSetStatusDetails,
                        mediaId =  fileSetWithDownloads.fileSet.mediaId,
                        artworkUrl = artFile ?: "",
                        artworkPoster = artworkPoster
                    )
                )

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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateTimerWork()
            } catch (ex: Exception) {
                Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
                ex.logToCrashlytics()
            }
            _updateTimerBusy = false
        }
    }

    private suspend fun updateTimerWork() {

        //Don't do anything until we know what to do
        if(_profileId == 0)
            return

        if(!networkManager.isConnected())
            return

        //Cleanup orphaned downloads
        val jobs = _db.getJobs(_profileId)
        var jobFileSetMTMs = _db.getJobFileSetMTMs()
        for(jobFileSetMTM in jobFileSetMTMs) {
            val valid = jobs.any{it.mediaId == jobFileSetMTM.jobMediaId && it.mediaType == jobFileSetMTM.jobMediaType }
            if(!valid)
                _db.delete(jobFileSetMTM)
        }


        jobFileSetMTMs = _db.getJobFileSetMTMs()
        val fileSets = _db.getFileSets()
        for(fileSet in fileSets) {
            val valid = jobFileSetMTMs.any {
                it.fileSetMediaId == fileSet.mediaId
            }
            if(!valid)
                _db.delete(fileSet)
        }


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
                ex.logToCrashlytics()
                ex.printStackTrace()
            }
        }

    }

    private fun saveFile(fileName: String, data: Any) {
        val file = File(_rootDir, fileName)
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

            val size = getSize(url)
            dl = Download(
                url = url,
                totalBytes = size,
                fileName = fileName,
                mediaId = fileSetWithDownloads.fileSet.mediaId,
                status = if(size > 0) DownloadStatus.None else DownloadStatus.Pending,
                statusDetails = "",
                disposition = disposition
            )

            _db.insert(download = dl)

        } else {

            if (dl.fileName != fileName || dl.url != url) {
                val size = getSize(url)

                dl.url = url
                dl.fileName = fileName
                dl.complete = false
                dl.totalBytes = size
                dl.complete = false
                dl.androidId = 0
                dl.downloadedBytes = 0
                dl.status = if(size > 0) DownloadStatus.None else DownloadStatus.Pending
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
            val size = getSize(sub.url)
            dl = Download(
                url = sub.url,
                totalBytes = size,
                fileName = fileName,
                mediaId = fileSetWithDownloads.fileSet.mediaId,
                status = if(size > 0) DownloadStatus.None else DownloadStatus.Pending,
                statusDetails = "",
                disposition = DISPOSITION_SUBTITLE
            )
            _db.insert(download = dl)
        } else {
            if (dl.fileName != fileName || dl.url != sub.url) {
                val size = getSize(sub.url)
                dl.url = sub.url
                dl.fileName = fileName
                dl.complete = false
                dl.totalBytes = size
                dl.complete = false
                dl.androidId = 0
                dl.downloadedBytes = 0
                dl.status = if(size > 0) DownloadStatus.None else DownloadStatus.Pending
                dl.statusDetails = ""
                _db.update(download = dl)
            }
        }

    }

    private suspend fun updateMovie(job: Job) {

        val detailedMovie = moviesRepository.details(id = job.mediaId)
        saveFile(fileName = "${detailedMovie.id}.${job.mediaType.asString()}.json", data = detailedMovie)

        var fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)
        if(fileSetWithDownloads == null) {
            _db.insert(
                FileSet(
                    mediaId = job.mediaId,
                    title = detailedMovie.displayTitle()
                )
            )
            fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)!!
        }

        if(_db.getJobFileSetMTM(job.mediaId, job.mediaType, fileSetWithDownloads.fileSet.mediaId) == null) {
            _db.insert(
                jobFileSetMTM = JobFileSetMTM(
                    jobMediaId = job.mediaId,
                    jobMediaType = job.mediaType,
                    fileSetMediaId = job.mediaId
                )
            )
        }


        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedMovie.artworkUrl, disposition = DISPOSITION_POSTER)
        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedMovie.backdropUrl, disposition = DISPOSITION_BACKDROP)
        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedMovie.bifUrl, disposition = DISPOSITION_BIF)

        if(detailedMovie.externalSubtitles?.isNotEmpty() == true) {
            for(sub in detailedMovie.externalSubtitles) {
                addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSetWithDownloads, sub = sub)
            }
        }

        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedMovie.videoUrl, disposition = DISPOSITION_VIDEO)

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }

    private suspend fun updateSeries(job: Job) {

        val detailedSeries = seriesRepository.details(job.mediaId)
        saveFile(fileName = "${detailedSeries.id}.${job.mediaType.asString()}.json", data = detailedSeries)

        var fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)
        if(fileSetWithDownloads == null) {
            _db.insert(
                FileSet(
                    mediaId = job.mediaId,
                    title = detailedSeries.title
                )
            )
            fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)!!
        }

        //Used for tracking orphaned downloads
        if(_db.getJobFileSetMTM(job.mediaId, job.mediaType, fileSetWithDownloads.fileSet.mediaId) == null) {
            _db.insert(
                jobFileSetMTM = JobFileSetMTM(
                    jobMediaId = job.mediaId,
                    jobMediaType = job.mediaType,
                    fileSetMediaId = job.mediaId
                )
            )
        }

        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedSeries.artworkUrl, disposition = DISPOSITION_POSTER)
        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedSeries.backdropUrl, disposition = DISPOSITION_BACKDROP)


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
        val jobMTMs = _db.getJobFileSetMTMs(jobMediaId = job.mediaId, jobMediaType = job.mediaType)
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
                if (_db.getJobFileSetMTM(job.mediaId, job.mediaType, episode.id) == null) {
                    _db.insert(
                        jobFileSetMTM = JobFileSetMTM(
                            jobMediaId = job.mediaId,
                            jobMediaType = job.mediaType,
                            fileSetMediaId = episode.id
                        )
                    )
                }

                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = episode.artworkUrl, disposition = DISPOSITION_SCREENSHOT)
                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = episode.bifUrl, disposition = DISPOSITION_BIF)

                if (episode.externalSubtitles != null) {
                    for (sub in episode.externalSubtitles)
                        addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSetWithDownloads, sub = sub)
                }

                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = episode.videoUrl, disposition = DISPOSITION_VIDEO)

            }
        }

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }

    private suspend fun updateEpisode(job: Job) {

        val detailedEpisode = episodesRepository.details(id = job.mediaId)
        saveFile(fileName = "${detailedEpisode.id}.${job.mediaType.asString()}.json", data = detailedEpisode)

        var fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)
        if(fileSetWithDownloads == null) {
            _db.insert(
                FileSet(
                    mediaId = job.mediaId,
                    title = detailedEpisode.fullDisplayTitle()
                )
            )
            fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)!!
        }

        if(_db.getJobFileSetMTM(job.mediaId, job.mediaType, fileSetWithDownloads.fileSet.mediaId) == null) {
            _db.insert(
                jobFileSetMTM = JobFileSetMTM(
                    jobMediaId = job.mediaId,
                    jobMediaType = job.mediaType,
                    fileSetMediaId = job.mediaId
                )
            )
        }


        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedEpisode.artworkUrl, disposition = DISPOSITION_SCREENSHOT)
        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedEpisode.bifUrl, disposition = DISPOSITION_BIF)

        if(detailedEpisode.externalSubtitles?.isNotEmpty() == true) {
            for(sub in detailedEpisode.externalSubtitles) {
                addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSetWithDownloads, sub = sub)
            }
        }

        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedEpisode.videoUrl, disposition = DISPOSITION_VIDEO)

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)

    }

    private suspend fun updatePlaylist(job: Job) {

        val detailedPlaylist = playlistRepository.details(job.mediaId)
        saveFile(fileName = "${detailedPlaylist.id}.${job.mediaType.asString()}.json", data = detailedPlaylist)

        var fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)
        if(fileSetWithDownloads == null) {
            _db.insert(
                FileSet(
                    mediaId = job.mediaId,
                    title = detailedPlaylist.name
                )
            )
            fileSetWithDownloads = _db.getFileSet(mediaId = job.mediaId)!!
        }

        //Used for tracking orphaned downloads
        if(_db.getJobFileSetMTM(job.mediaId, job.mediaType, fileSetWithDownloads.fileSet.mediaId) == null) {
            _db.insert(
                jobFileSetMTM = JobFileSetMTM(
                    jobMediaId = job.mediaId,
                    jobMediaType = job.mediaType,
                    fileSetMediaId = job.mediaId
                )
            )
        }

        addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = detailedPlaylist.artworkUrl, disposition = DISPOSITION_POSTER)


        //Identify ids of items that should be downloaded
        var upNext = detailedPlaylist.items?.firstOrNull {
            it.index == detailedPlaylist.currentItemId
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
        val jobMTMs = _db.getJobFileSetMTMs(jobMediaId = job.mediaId, jobMediaType = job.mediaType)
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
                if (_db.getJobFileSetMTM(job.mediaId, job.mediaType, playlistItem.id) == null) {
                    _db.insert(
                        jobFileSetMTM = JobFileSetMTM(
                            jobMediaId = job.mediaId,
                            jobMediaType = job.mediaType,
                            fileSetMediaId = playlistItem.id
                        )
                    )
                }

                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = playlistItem.artworkUrl, disposition = DISPOSITION_SCREENSHOT)
                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = playlistItem.bifUrl, disposition = DISPOSITION_BIF)

                if (playlistItem.externalSubtitles != null) {
                    for (sub in playlistItem.externalSubtitles)
                        addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSetWithDownloads, sub = sub)
                }

                addOrUpdateDownload(fileSetWithDownloads = fileSetWithDownloads, url = playlistItem.videoUrl, disposition = DISPOSITION_VIDEO)

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
                profileId = _profileId,
                title = detailedMovie.displayTitle(),
                count = 1,
                pending = true,
                lastUpdate = lastUpdate.time
            )
        )
    }

    suspend fun addOrUpdateSeries(detailedSeries: DetailedSeries, count: Int) {

        if(count == 0) {
            delete(detailedSeries.id, MediaTypes.Series)
            return
        }

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = _db.getJob(detailedSeries.id, MediaTypes.Series, _profileId)
        if(job == null) {
            _db.insert(
                Job(
                    mediaId = detailedSeries.id,
                    mediaType = MediaTypes.Series,
                    profileId = _profileId,
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

    suspend fun updateSeries(mediaId: Int, newCount: Int) {
        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = _db.getJob(mediaId, MediaTypes.Series, _profileId)
        if (job != null) {
            if(newCount == 0) {
                _db.delete(job)
            } else if(job.count != newCount) {
                job.count = newCount
                job.lastUpdate = lastUpdate.time
                _db.update(job)
            }
        }
    }

    suspend fun addEpisode(detailedEpisode: DetailedEpisode) {

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        _db.insert(
            Job(
                mediaId = detailedEpisode.id,
                mediaType = MediaTypes.Episode,
                profileId = _profileId,
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

        val job = _db.getJob(detailedPlaylist.id, MediaTypes.Playlist, _profileId)
        if(job == null) {
            _db.insert(
                Job(
                    mediaId = detailedPlaylist.id,
                    mediaType = MediaTypes.Playlist,
                    profileId = _profileId,
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

    suspend fun updatePlaylist(mediaId: Int, newCount: Int) {
        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = _db.getJob(mediaId, MediaTypes.Playlist, _profileId)
        if (job != null) {
            if(newCount == 0) {
                _db.delete(job)
            } else if(job.count != newCount) {
                job.count = newCount
                job.lastUpdate = lastUpdate.time
                _db.update(job)
            }
        }
    }

    suspend fun delete(mediaId: Int, mediaType: MediaTypes) {
        val job = _db.getJob(mediaId, mediaType, _profileId)
        if (job != null)
            _db.delete(job)
    }

    suspend fun deleteAll() {

        /**
         * No idea why, but this throws a main ui thread exception
         */
//        _db.deleteAllJobs(_profileId)

        /**
         * So do it the dumb-ass way, it works
         */
        val jobs = _db.getJobs(_profileId)
        for(job in jobs)
            _db.delete(job)
    }

    private fun getLocalFile(filename: String): String? {
        val file = _rootDir.resolve(filename)
        if(file.exists())
            return file.absolutePath
        return null
    }

    fun getLocalVideo(mediaId: Int, ext: String) =
        getLocalFile("${mediaId}.$DISPOSITION_VIDEO$ext")


    fun getLocalSubtitle(mediaId: Int, ext: String) =
        getLocalFile("${mediaId}.$DISPOSITION_SUBTITLE$ext")

    fun getLocalPoster(mediaId: Int, ext: String) =
        getLocalFile("${mediaId}.$DISPOSITION_POSTER$ext")
}