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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.SRTSubtitles
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.repositories.EpisodesRepository
import tv.dustypig.dustypig.api.repositories.MoviesRepository
import tv.dustypig.dustypig.api.repositories.PlaylistRepository
import tv.dustypig.dustypig.api.repositories.SeriesRepository
import tv.dustypig.dustypig.global_managers.NetworkManager
import tv.dustypig.dustypig.global_managers.PlayerStateManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics
import java.io.File
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
//        private const val DISPOSITION_BIF = "bif"
        private const val DISPOSITION_SUBTITLE = "subtitle"
        private const val DISPOSITION_INFO = "info"
        private const val NO_MEDIA = ".nomedia"
    }

    private val _androidDownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as AndroidDownloadManager
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

    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var _playerScreenVisible = false
    private var _playbackId = 0

    private val displayMetrics = Resources.getSystem().displayMetrics
    private val urlParameters = "displayWidth=${displayMetrics.widthPixels}&displayHeight=${displayMetrics.heightPixels}"


    private var _doFullScan = false

    init {

        _rootDir.mkdirs()
        _scope.launch {
            withContext(Dispatchers.IO) {
                File(_rootDir, NO_MEDIA).createNewFile()
            }
        }

        _scope.launch {
            settingsManager.profileIdFlow.collectLatest { _profileId = it }
        }

        _scope.launch {
            settingsManager.downloadOverMobileFlow.collectLatest { _downloadOverMobile = it }
        }

        _scope.launch {
            PlayerStateManager.playerScreenVisible.collectLatest { _playerScreenVisible = it  }
        }

        _scope.launch {
            PlayerStateManager.playbackId.collectLatest { _playbackId = it }
        }

        _statusTimer.schedule(
            delay = 1000,
            period = 250
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

    private fun statusTimerTick() {
        if(_statusTimerBusy)
            return
        _statusTimerBusy = true
        _scope.launch {
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
        if (_profileId <= 0)
            return

        //No downloads without a network
        if (!networkManager.isConnected())
            return


        //Load from DB
        val jobs = _db.getJobs(_profileId)
        val downloads = _db.getDownloads(_profileId)

        //This uses less battery by only scanning ADM and disk files if necessary
        val pendingDownloads = downloads.any { !it.complete }
        if (!(pendingDownloads || _doFullScan))
            return


        _doFullScan = false
        val cursor = _androidDownloadManager.query(AndroidDownloadManager.Query())
        while (cursor.moveToNext()) {
            _doFullScan = true

            val androidId = getLong(cursor, AndroidDownloadManager.COLUMN_ID)
            val download = downloads.firstOrNull { it.androidId == androidId }
            if (download == null) {
                //No longer valid: Remove
                _androidDownloadManager.remove(androidId)
            } else {
                var saveChanges = false
                val totalBytes = getLong(cursor, AndroidDownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                if (totalBytes > 0) {
                    download.totalBytes = totalBytes
                    download.downloadedBytes =
                        getLong(cursor, AndroidDownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    saveChanges = true
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
                        saveChanges = true
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
                        saveChanges = true
                    }

                    if (statusReasonCode == AndroidDownloadManager.ERROR_INSUFFICIENT_SPACE && download.totalBytes > 0) {
                        //Retry when enough space available
                        val stat = StatFs(_rootDir.path)
                        if (download.totalBytes < stat.availableBytes) {
                            _androidDownloadManager.remove(androidId)
                            download.androidId = 0
                            download.lastRetry = Date()
                            saveChanges = true
                        }
                    } else {
                        //Retry once every 10 seconds
                        if (download.lastRetry.secondsSince() >= 10) {
                            _androidDownloadManager.remove(androidId)
                            download.androidId = 0
                            download.lastRetry = Date()
                            saveChanges = true
                        }
                    }

                } else {

                    var status = when (statusCode) {
                        AndroidDownloadManager.STATUS_RUNNING -> DownloadStatus.Running
                        AndroidDownloadManager.STATUS_PAUSED -> DownloadStatus.Paused
                        else -> DownloadStatus.Pending
                    }

                    if (status == DownloadStatus.Paused && statusReasonCode == AndroidDownloadManager.PAUSED_WAITING_TO_RETRY) {
                        status = DownloadStatus.Running
                    }

                    if (download.status != status) {
                        download.status = status
                        saveChanges = true
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
                            saveChanges = true
                        }
                    }
                }

                if (saveChanges) {

                    //Multiple jobs can have the same file
                    for (dl in downloads.filter { it.fileName == download.fileName }) {
                        dl.androidId = download.androidId
                        dl.totalBytes = download.totalBytes
                        dl.downloadedBytes = download.downloadedBytes
                        dl.complete = download.complete
                        dl.status = download.status
                        dl.statusDetails = download.statusDetails
                        dl.lastRetry = download.lastRetry
                        _db.update(dl)
                    }
                }
            }
        }
        cursor.close()


        if (_playerScreenVisible)
            _doFullScan = true

        //Remove any files that are invalid (but not during playback)
        for (filename in _rootDir.list() ?: arrayOf()) {
            var valid = filename == NO_MEDIA

            if (!valid && _playerScreenVisible && _playbackId > 0) {
                valid = _rootDir.resolve(filename).absolutePath.startsWith("$_playbackId.")
            }

            if (!valid) {
                for (job in jobs) {
                    if (filename == job.filename) {
                        valid = true
                        break
                    }
                }

            }
            if (!valid) {
                for (d in downloads) {
                    if (filename == d.fileName || filename == "${d.fileName}.tmp") {
                        valid = true
                        break
                    }
                }
            }

            if (!valid) {
                File(_rootDir, filename).delete()
            }
        }


        //Add any support files that are not started (max of 3)
        val runningSupportFiles = downloads.filter {
            it.androidId != 0L && it.disposition != DISPOSITION_VIDEO && !it.complete
        }
        if (runningSupportFiles.count() < 3) {
            val nextDownload =
                downloads.firstOrNull { it.androidId == 0L && it.disposition != DISPOSITION_VIDEO }
            if (nextDownload != null) {
                var file = File(_rootDir.absolutePath + "/${nextDownload.fileName}")
                if (file.exists())
                    file.delete()
                file = File(_rootDir, "${nextDownload.fileName}.tmp")
                if (file.exists())
                    file.delete()

                var url = nextDownload.url
                if (listOf(
                        DISPOSITION_POSTER,
                        DISPOSITION_BACKDROP,
                        DISPOSITION_SCREENSHOT
                    ).contains(nextDownload.disposition)
                ) {
                    url += if (url.contains("?")) "&$urlParameters" else "?$urlParameters"
                }

                val uri = android.net.Uri.parse(url)
                val request = android.app.DownloadManager.Request(uri)
                request.setDestinationUri(android.net.Uri.fromFile(file))
                request.setAllowedOverMetered(_downloadOverMobile)
                request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_HIDDEN)
                nextDownload.androidId = _androidDownloadManager.enqueue(request)

                //Multiple jobs can have the same file
                for (dl in downloads.filter { it.fileName == nextDownload.fileName }) {
                    dl.androidId = nextDownload.androidId
                    _db.update(dl)
                }
            }
        }

        //Only download 1 video at a time
        val runningVideos = downloads.filter {
            it.androidId != 0L && it.disposition == DISPOSITION_VIDEO && !it.complete
        }
        if (runningVideos.isEmpty()) {
            val nextDownload =
                downloads.firstOrNull { it.androidId == 0L && it.disposition == DISPOSITION_VIDEO }
            if (nextDownload != null) {
                var file = File(_rootDir.absolutePath + "/${nextDownload.fileName}")
                if (file.exists())
                    file.delete()
                file = File(_rootDir, "${nextDownload.fileName}.tmp")
                if (file.exists())
                    file.delete()

                var url = nextDownload.url
                url += if (url.contains("?")) "&$urlParameters" else "?$urlParameters"

                val uri = android.net.Uri.parse(url)
                val request = android.app.DownloadManager.Request(uri)
                request.setDestinationUri(android.net.Uri.fromFile(file))
                request.setAllowedOverMetered(_downloadOverMobile)
                request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_HIDDEN)
                nextDownload.androidId = _androidDownloadManager.enqueue(request)
                _db.update(nextDownload)

                //Multiple jobs can have the same file
                for (dl in downloads.filter { it.fileName == nextDownload.fileName }) {
                    dl.androidId = nextDownload.androidId
                    _db.update(dl)
                }
            }
        }


        //Update the flow
        if (!_playerScreenVisible) {
            val fileSets = _db.getAllFileSets(_profileId)
            val uiJobs = ArrayList<UIJob>()
            for (job in jobs) {

                var artworkPoster = false
                var artDL =
                    downloads.firstOrNull { it.mediaId == job.mediaId && it.disposition == DISPOSITION_SCREENSHOT }
                if (artDL == null)
                    artDL =
                        downloads.firstOrNull { it.mediaId == job.mediaId && it.disposition == DISPOSITION_BACKDROP }
                if (artDL == null) {
                    artDL =
                        downloads.firstOrNull { it.mediaId == job.mediaId && it.disposition == DISPOSITION_POSTER }
                    artworkPoster = artDL != null
                }

                var artFile = _rootDir.listFiles()?.firstOrNull { it.name == artDL?.fileName }?.path
                if (artFile == null) {
                    artFile = artDL?.url
                    artworkPoster = artDL?.disposition == DISPOSITION_POSTER
                }

                val uiJob = UIJob(
                    key = job.id.toString(),
                    mediaId = job.mediaId,
                    mediaType = job.mediaType,
                    title = job.title,
                    artworkUrl = artFile ?: "",
                    artworkPoster = artworkPoster,
                    percent = 0.0f,
                    status = if (job.pending) DownloadStatus.Pending else DownloadStatus.Finished,
                    statusDetails = "",
                    downloads = listOf(),
                    count = job.count
                )
                uiJobs.add(uiJob)

                var jobTotalSize = 0L
                var jobCompleted = 0L
                val uiDownloads = ArrayList<UIDownload>()

                for (fileSet in fileSets.filter { it.jobId == job.id }.sortedBy { it.playOrder }) {
                    var fileSetTotalSize = 0L
                    var fileSetCompleted = 0L
                    var fileSetStatus = DownloadStatus.Finished
                    var fileSetStatusDetails = ""
                    for (download in downloads.filter { it.fileSetId == fileSet.id }) {

                        if (download.totalBytes < 0) {
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
                    artDL =
                        downloads.firstOrNull { it.fileSetId == fileSet.id && it.disposition == DISPOSITION_SCREENSHOT }
                    if (artDL == null)
                        artDL =
                            downloads.firstOrNull { it.fileSetId == fileSet.id && it.disposition == DISPOSITION_BACKDROP }
                    if (artDL == null) {
                        artDL =
                            downloads.firstOrNull { it.fileSetId == fileSet.id && it.disposition == DISPOSITION_POSTER }
                        artworkPoster = artDL != null
                    }

                    artFile = _rootDir.listFiles()?.firstOrNull { it.name == artDL?.fileName }?.path
                    if (artFile == null) {
                        artFile = artDL?.url
                        artworkPoster = artDL?.disposition == DISPOSITION_POSTER
                    }

                    uiDownloads.add(
                        UIDownload(
                            key = "${job.id}.${fileSet.id}",
                            title = fileSet.title,
                            percent = if (fileSetTotalSize > 0) fileSetCompleted.toFloat() / fileSetTotalSize.toFloat() else 0.0f,
                            status = fileSetStatus,
                            statusDetails = fileSetStatusDetails,
                            mediaId = if (job.mediaType == MediaTypes.Playlist) fileSet.playlistItemId else fileSet.mediaId,
                            artworkUrl = artFile ?: "",
                            artworkPoster = artworkPoster
                        )
                    )

                    jobTotalSize += fileSetTotalSize.coerceAtLeast(0L)
                    jobCompleted += fileSetCompleted.coerceAtLeast(0L)
                }

                uiJob.percent =
                    if (jobTotalSize > 0) jobCompleted.toFloat() / jobTotalSize.toFloat() else 0.0f
                uiJob.downloads = uiDownloads.toList()
            }

            _downloadFlow.tryEmit(uiJobs.toList())
        }
    }



    private fun updateTimerTick() {
        if(_updateTimerBusy)
            return
        _updateTimerBusy = true
        _scope.launch {
            try {
                updateTimerWork()
            } catch(ex: Exception) {
                Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
                ex.logToCrashlytics()
            }
            _updateTimerBusy = false
        }
    }

    private suspend fun updateTimerWork() {

        if(_profileId <= 0)
            return

        if(!networkManager.isConnected())
            return

        val jobs = _db.getJobs(_profileId)
        for (job in jobs) {
            try {
                var update = job.pending
                if (!update && (job.mediaType == MediaTypes.Series || job.mediaType == MediaTypes.Playlist)) {
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
                Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
                ex.logToCrashlytics()
            }
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


    private fun saveFile(fileName: String, data: Any) {
        val file = File(_rootDir, fileName)
        if(file.exists())
            file.delete()
        file.writeText(Gson().toJson(data))
    }

    private fun getDownloadFilename(
        mediaId: Int,
        isPlaylist: Boolean,
        disposition: String,
        ext: String
    ): String {
        val pls = if(isPlaylist) ".playlist" else ""
        return "$mediaId$pls.$disposition$ext"
    }

    private fun getJsonFilename(
        mediaId: Int,
        isPlaylist: Boolean = false
    ) = getDownloadFilename(
        mediaId = mediaId,
        isPlaylist = isPlaylist,
        disposition = DISPOSITION_INFO,
        ext = ".json"
    )

    private suspend fun addOrUpdateDownload(
        fileSetWithDownloads: FileSetWithDownloads,
        url: String?,
        disposition: String,
        size: Long,
        isPlaylist: Boolean = false
    ) {

        if(url.isNullOrBlank())
            return

        var ext = url.split("?")[0]
        ext = ext.substring(ext.lastIndexOf("."))
        if(ext.isBlank())
            ext = ".dat"


        val fileName = getDownloadFilename(
            mediaId = fileSetWithDownloads.fileSet.mediaId,
            isPlaylist = isPlaylist,
            disposition = disposition,
            ext = ext
        )

        var dl = fileSetWithDownloads.downloads.firstOrNull {
            it.fileName == fileName
        }

        if(dl == null) {
            dl = Download(
                fileSetId = fileSetWithDownloads.fileSet.id,
                url = url,
                totalBytes = size,
                fileName = fileName,
                mediaId = fileSetWithDownloads.fileSet.mediaId,
                status = if(size > 0) DownloadStatus.None else DownloadStatus.Pending,
                statusDetails = "",
                disposition = disposition,
                profileId = _profileId
            )

            _db.insert(download = dl)

        } else {

            if (dl.fileName != fileName || dl.url != url) {
                dl.url = url
                dl.fileName = fileName
                dl.complete = false
                dl.totalBytes = -size
                dl.complete = false
                dl.androidId = 0
                dl.downloadedBytes = 0
                dl.status = if(size > 0) DownloadStatus.None else DownloadStatus.Pending
                dl.statusDetails = ""

                _db.update(download = dl)
            }

        }
    }

    private suspend fun addOrUpdateSubtitleDownload(
        fileSetWithDownloads: FileSetWithDownloads,
        sub: SRTSubtitles
    ) {

        if(sub.url.isBlank())
            return

        var ext = sub.url.split("?")[0]
        ext = ext.substring(startIndex = ext.lastIndexOf(string = "."))
        if(ext.isBlank())
            ext = ".dat"

        val fileName = getDownloadFilename(
            mediaId = fileSetWithDownloads.fileSet.mediaId,
            isPlaylist = false,
            disposition = DISPOSITION_SUBTITLE,
            ext = ext
        )

        var dl = fileSetWithDownloads.downloads.firstOrNull {
            it.fileName == fileName
        }
        if(dl == null) {
            dl = Download(
                fileSetId = fileSetWithDownloads.fileSet.id,
                url = sub.url,
                totalBytes = sub.fileSize.toLong(),
                fileName = fileName,
                mediaId = fileSetWithDownloads.fileSet.mediaId,
                status = if(sub.fileSize > 0u) DownloadStatus.None else DownloadStatus.Pending,
                statusDetails = "",
                disposition = DISPOSITION_SUBTITLE,
                profileId = _profileId
            )
            _db.insert(download = dl)
        } else {
            if (dl.fileName != fileName || dl.url != sub.url) {
                dl.url = sub.url
                dl.fileName = fileName
                dl.complete = false
                dl.totalBytes = sub.fileSize.toLong()
                dl.complete = false
                dl.androidId = 0
                dl.downloadedBytes = 0
                dl.status = if(sub.fileSize > 0u) DownloadStatus.None else DownloadStatus.Pending
                dl.statusDetails = ""
                _db.update(download = dl)
            }
        }

    }

    private suspend fun updateMovie(job: Job) {

        val detailedMovie = moviesRepository.details(id = job.mediaId)
        saveFile(job.filename, detailedMovie)

        var fileSetWithDownloads = _db.getFileSetAndDownloadsByMediaId(jobId = job.id, mediaId = job.mediaId)
        if(fileSetWithDownloads == null) {
            _db.insert(
                FileSet(
                    jobId = job.id,
                    mediaId = job.mediaId,
                    title = detailedMovie.displayTitle(),
                    playOrder = 0,
                    profileId = _profileId
                )
            )
            fileSetWithDownloads = _db.getFileSetAndDownloadsByMediaId(jobId = job.id, mediaId = job.mediaId)!!
        }


        addOrUpdateDownload(
            fileSetWithDownloads = fileSetWithDownloads,
            url = detailedMovie.artworkUrl,
            disposition = DISPOSITION_POSTER,
            size = detailedMovie.artworkSize.toLong()
        )

        addOrUpdateDownload(
            fileSetWithDownloads = fileSetWithDownloads,
            url = detailedMovie.backdropUrl,
            disposition = DISPOSITION_BACKDROP,
            size = detailedMovie.backdropSize.toLong()
        )

//        addOrUpdateDownload(
//            fileSetWithDownloads = fileSetWithDownloads,
//            url = detailedMovie.bifUrl,
//            disposition = DISPOSITION_BIF,
//            size = detailedMovie.bifSize.toLong()
//        )

        if(detailedMovie.srtSubtitles?.isNotEmpty() == true) {
            for(sub in detailedMovie.srtSubtitles) {
                addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSetWithDownloads, sub = sub)
            }
        }

        addOrUpdateDownload(
            fileSetWithDownloads = fileSetWithDownloads,
            url = detailedMovie.videoUrl,
            disposition = DISPOSITION_VIDEO,
            size = detailedMovie.videoSize.toLong()
        )

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }

    private suspend fun updateSeries(job: Job) {

        val detailedSeries = seriesRepository.details(job.mediaId)
        saveFile(job.filename, detailedSeries)


        var jobFileSet = _db.getFileSetAndDownloadsByMediaId(jobId = job.id, mediaId = job.mediaId)
        if(jobFileSet == null) {
            _db.insert(
                FileSet(
                    jobId = job.id,
                    mediaId = job.mediaId,
                    title = detailedSeries.title,
                    playOrder = 0,
                    profileId = _profileId
                )
            )
            jobFileSet = _db.getFileSetAndDownloadsByMediaId(jobId = job.id, mediaId = job.mediaId)!!
        }


        addOrUpdateDownload(
            fileSetWithDownloads = jobFileSet,
            url = detailedSeries.artworkUrl,
            disposition = DISPOSITION_POSTER,
            size = detailedSeries.artworkSize.toLong()
        )

        addOrUpdateDownload(
            fileSetWithDownloads = jobFileSet,
            url = detailedSeries.backdropUrl,
            disposition = DISPOSITION_BACKDROP,
            size = detailedSeries.backdropSize.toLong()
        )


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
        val fileSets = _db.getFileSets(jobId = job.id)
        for(fileSet in fileSets) {
            if(fileSet.mediaId != job.mediaId) {
                if (!itemIds.any {
                        it == fileSet.mediaId
                    }) {
                    _db.delete(fileSet)
                }
            }
        }



        //Add any episodes that should be downloaded
        if(upNext != null) {
            for ((idx, mediaId) in itemIds.withIndex()) {
                val episode = detailedSeries.episodes!!.first {
                    it.id == mediaId
                }
                var fileSet = _db.getFileSetAndDownloadsByMediaId(jobId = job.id, mediaId = episode.id)
                if (fileSet == null) {
                    _db.insert(
                        FileSet(
                            jobId = job.id,
                            mediaId = episode.id,
                            title = episode.fullDisplayTitle(),
                            playOrder = idx,
                            profileId = _profileId
                        )
                    )
                    fileSet = _db.getFileSetAndDownloadsByMediaId(jobId = job.id, mediaId = episode.id)!!
                } else if(fileSet.fileSet.playOrder != idx) {
                    fileSet.fileSet.playOrder = idx
                    _db.update(fileSet.fileSet)
                }

                addOrUpdateDownload(
                    fileSetWithDownloads = fileSet,
                    url = episode.artworkUrl,
                    disposition = DISPOSITION_SCREENSHOT,
                    size = episode.artworkSize.toLong()
                )

//                addOrUpdateDownload(
//                    fileSetWithDownloads = fileSet,
//                    url = episode.bifUrl,
//                    disposition = DISPOSITION_BIF,
//                    size = episode.bifSize.toLong()
//                )

                if (episode.srtSubtitles != null) {
                    for (sub in episode.srtSubtitles)
                        addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSet, sub = sub)
                }

                addOrUpdateDownload(
                    fileSetWithDownloads = fileSet,
                    url = episode.videoUrl,
                    disposition = DISPOSITION_VIDEO,
                    size = episode.videoSize.toLong()
                )

            }
        }

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }

    private suspend fun updateEpisode(job: Job) {

        val detailedEpisode = episodesRepository.details(id = job.mediaId)
        saveFile(job.filename, detailedEpisode)

        var fileSetWithDownloads = _db.getFileSetAndDownloadsByMediaId(jobId = job.id, mediaId = job.mediaId)
        if(fileSetWithDownloads == null) {
            _db.insert(
                FileSet(
                    jobId = job.id,
                    mediaId = job.mediaId,
                    title = detailedEpisode.fullDisplayTitle(),
                    playOrder = 0,
                    profileId = _profileId
                )
            )
            fileSetWithDownloads = _db.getFileSetAndDownloadsByMediaId(jobId = job.id, mediaId = job.mediaId)!!
        }

        addOrUpdateDownload(
            fileSetWithDownloads = fileSetWithDownloads,
            url = detailedEpisode.artworkUrl,
            disposition = DISPOSITION_SCREENSHOT,
            size = detailedEpisode.artworkSize.toLong()
        )

//        addOrUpdateDownload(
//            fileSetWithDownloads = fileSetWithDownloads,
//            url = detailedEpisode.bifUrl,
//            disposition = DISPOSITION_BIF,
//            size = detailedEpisode.bifSize.toLong()
//        )

        if(detailedEpisode.srtSubtitles?.isNotEmpty() == true) {
            for(sub in detailedEpisode.srtSubtitles) {
                addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSetWithDownloads, sub = sub)
            }
        }

        addOrUpdateDownload(
            fileSetWithDownloads = fileSetWithDownloads,
            url = detailedEpisode.videoUrl,
            disposition = DISPOSITION_VIDEO,
            size = detailedEpisode.videoSize.toLong()
        )

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }

    private suspend fun updatePlaylist(job: Job) {

        val detailedPlaylist = playlistRepository.details(job.mediaId)
        saveFile(job.filename, detailedPlaylist)

        var jobFileSet = _db.getFileSetAndDownloadsByMediaId(jobId = job.id, mediaId = job.mediaId)
        if(jobFileSet == null) {
            _db.insert(
                FileSet(
                    jobId = job.id,
                    mediaId = job.mediaId,
                    title = detailedPlaylist.name,
                    playOrder = 0,
                    profileId = _profileId
                )
            )
            jobFileSet = _db.getFileSetAndDownloadsByMediaId(jobId = job.id, mediaId = job.mediaId)!!
        }


        addOrUpdateDownload(
            fileSetWithDownloads = jobFileSet,
            url = detailedPlaylist.artworkUrl,
            disposition = DISPOSITION_POSTER,
            size = detailedPlaylist.artworkSize.toLong(),
            isPlaylist = true
        )


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
            for (playListItem in detailedPlaylist.items!!) {
                if (upNext.id == playListItem.id)
                    reachedUpNext = true
                if (reachedUpNext) {
                    itemIds.add(playListItem.id)
                    count++
                    if (count >= job.count)
                        break
                }
            }
        }

        //Remove any items that should no longer be downloaded
        val fileSets = _db.getFileSets(jobId = job.id)
        for(fileSet in fileSets) {
            if(fileSet.mediaId != job.mediaId) {
                if (!itemIds.any {
                        it == fileSet.playlistItemId
                    }) {
                    _db.delete(fileSet)
                }
            }
        }



        //Add any items that should be downloaded
        if(upNext != null) {
            for ((idx, playlistItemId) in itemIds.withIndex()) {
                val playlistItem = detailedPlaylist.items!!.first {
                    it.id == playlistItemId
                }
                var fileSet = _db.getFileSetAndDownloadsByPlaylistItemId(jobId = job.id, playlistItemId = playlistItem.id)
                if (fileSet == null) {
                    _db.insert(
                        FileSet(
                            jobId = job.id,
                            playlistItemId = playlistItem.id,
                            mediaId = playlistItem.mediaId,
                            title = playlistItem.title,
                            playOrder = idx,
                            profileId = _profileId
                        )
                    )
                    fileSet = _db.getFileSetAndDownloadsByPlaylistItemId(jobId = job.id, playlistItemId = playlistItem.id)!!
                } else if(fileSet.fileSet.playOrder != idx) {
                    fileSet.fileSet.playOrder = idx
                    _db.update(fileSet.fileSet)
                }

                addOrUpdateDownload(
                    fileSetWithDownloads = fileSet,
                    url = playlistItem.artworkUrl,
                    disposition = DISPOSITION_SCREENSHOT,
                    size = playlistItem.artworkSize.toLong()
                )

//                addOrUpdateDownload(
//                    fileSetWithDownloads = fileSet,
//                    url = playlistItem.bifUrl,
//                    disposition = DISPOSITION_BIF,
//                    size = playlistItem.bifSize.toLong()
//                )

                if (playlistItem.srtSubtitles != null) {
                    for (sub in playlistItem.srtSubtitles)
                        addOrUpdateSubtitleDownload(fileSetWithDownloads = fileSet, sub = sub)
                }

                addOrUpdateDownload(
                    fileSetWithDownloads = fileSet,
                    url = playlistItem.videoUrl,
                    disposition = DISPOSITION_VIDEO,
                    size = playlistItem.videoSize.toLong()
                )
            }
        }

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }



    suspend fun addMovie(detailedMovie: DetailedMovie) {

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = Job(
            mediaId = detailedMovie.id,
            mediaType = MediaTypes.Movie,
            profileId = _profileId,
            title = detailedMovie.displayTitle(),
            count = 1,
            pending = true,
            lastUpdate = lastUpdate.time,
            filename = getJsonFilename(detailedMovie.id)
        )

        _db.insert(job)
        saveFile(job.filename, job)
    }

    suspend fun addOrUpdateSeries(detailedSeries: DetailedSeries, count: Int) {

        if(count == 0) {
            delete(detailedSeries.id, MediaTypes.Series)
            return
        }

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        var job = _db.getJob(detailedSeries.id, MediaTypes.Series, _profileId)
        if(job == null) {

            job = Job(
                mediaId = detailedSeries.id,
                mediaType = MediaTypes.Series,
                profileId = _profileId,
                title = detailedSeries.title,
                count = count,
                pending = true,
                lastUpdate = lastUpdate.time,
                filename = getJsonFilename(detailedSeries.id)
            )

            _db.insert(job)
            saveFile(job.filename, job)

        } else if(job.count != count) {
            job.pending = true
            job.count = count
            _db.update(job)
        }

    }

    suspend fun updateSeries(mediaId: Int, newCount: Int) {

        if(newCount == 0) {
            delete(mediaId, MediaTypes.Series)
            return
        }

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = _db.getJob(mediaId, MediaTypes.Series, _profileId)
        if (job != null) {
            if(job.count != newCount) {
                job.count = newCount
                job.pending = true
                _db.update(job)
            }
        }
    }

    suspend fun addEpisode(detailedEpisode: DetailedEpisode) {

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = Job(
            mediaId = detailedEpisode.id,
            mediaType = MediaTypes.Episode,
            profileId = _profileId,
            title = "S${detailedEpisode.seasonNumber}:${detailedEpisode.episodeNumber}: ${detailedEpisode.title}",
            count = 1,
            pending = true,
            lastUpdate = lastUpdate.time,
            filename = getJsonFilename(detailedEpisode.id)
        )

        _db.insert(job)
        saveFile(job.filename, job)
    }

    suspend fun addOrUpdatePlaylist(detailedPlaylist: DetailedPlaylist, count: Int) {

        if(count == 0) {
            delete(detailedPlaylist.id, MediaTypes.Playlist)
            return
        }

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        var job = _db.getJob(detailedPlaylist.id, MediaTypes.Playlist, _profileId)
        if(job == null) {

            job = Job(
                mediaId = detailedPlaylist.id,
                mediaType = MediaTypes.Playlist,
                profileId = _profileId,
                title = detailedPlaylist.name,
                count = count,
                pending = true,
                lastUpdate = lastUpdate.time,
                filename = getJsonFilename(detailedPlaylist.id, true)
            )

            _db.insert(job)
            saveFile(job.filename, job)

        } else if(job.count != count) {
            job.count = count
            job.pending = true
            _db.update(job)
        }
    }

    suspend fun updatePlaylist(mediaId: Int, newCount: Int) {

        if(newCount == 0) {
            delete(mediaId, MediaTypes.Playlist)
            return
        }

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = _db.getJob(mediaId, MediaTypes.Playlist, _profileId)
        if (job != null) {
            if(job.count != newCount) {
                job.count = newCount
                job.pending = true
                _db.update(job)
            }
        }
    }

    suspend fun delete(mediaId: Int, mediaType: MediaTypes) {
        val job = _db.getJob(mediaId, mediaType, _profileId)
        if (job != null) {
            _db.delete(job)
        }
    }

    fun deleteAll() {

        /**
         * No idea why, but this throws a main ui thread exception, so scope it
         */
        _scope.launch {
            _db.deleteAllJobs(_profileId)
        }
    }

    private fun getLocalFile(filename: String): String? {
        val file = _rootDir.resolve(filename)
        if(file.exists())
            return file.absolutePath
        return null
    }

    fun getLocalVideo(mediaId: Int, ext: String) =
        getLocalFile(
            getDownloadFilename(
                mediaId = mediaId,
                isPlaylist = false,
                disposition = DISPOSITION_VIDEO,
                ext = ext
            )
        )

    fun getLocalSubtitle(mediaId: Int, ext: String) =
        getLocalFile(
            getDownloadFilename(
                mediaId = mediaId,
                isPlaylist = false,
                disposition = DISPOSITION_SUBTITLE,
                ext = ext
            )
        )

    fun getLocalPoster(mediaId: Int, isPlaylist: Boolean, ext: String) =
        getLocalFile(
            getDownloadFilename(
                mediaId = mediaId,
                isPlaylist = isPlaylist,
                disposition = DISPOSITION_POSTER,
                ext = ext
            )
        )

    fun loadDetailedMovie(mediaId: Int): DetailedMovie {
        val file = File(_rootDir, getJsonFilename(mediaId))
        return Gson().fromJson(file.readText(), DetailedMovie::class.java)
    }

    fun loadDetailedSeries(mediaId: Int): DetailedSeries {
        val file = File(_rootDir, getJsonFilename(mediaId))
        return Gson().fromJson(file.readText(), DetailedSeries::class.java)
    }

    fun loadDetailedEpisode(mediaId: Int): DetailedEpisode {
        val file = File(_rootDir, getJsonFilename(mediaId))
        return Gson().fromJson(file.readText(), DetailedEpisode::class.java)
    }

    fun loadDetailedPlaylist(playlistId: Int): DetailedPlaylist {
        val file = File(_rootDir, getJsonFilename(playlistId, true))
        return Gson().fromJson(file.readText(), DetailedPlaylist::class.java)
    }
}















