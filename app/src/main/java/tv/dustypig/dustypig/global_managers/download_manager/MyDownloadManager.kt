package tv.dustypig.dustypig.global_managers.download_manager

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Requirements
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.DetailedSeries
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

@UnstableApi
@Singleton
class MyDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager,
    private val networkManager: NetworkManager,
    private val moviesRepository: MoviesRepository,
    private val seriesRepository: SeriesRepository,
    private val episodesRepository: EpisodesRepository,
    private val playlistRepository: PlaylistRepository
) {

    companion object {
        private const val TAG = "MyDownloadManager"
        private const val UPDATE_MINUTES = 5
        private const val NO_MEDIA = ".nomedia"
    }

    private val _androidDownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as AndroidDownloadManager

    private val _downloadedFilesDir = MyDownloadService.getDownloadedFilesDirectory(context)
    private var _profileId = 0

    private val _statusTimer = Timer()
    private val _statusTimerMutex = Mutex(locked = false)

    private val _updateTimer = Timer()
    private val _updateTimerMutex = Mutex(locked = false)

    private val _currentDownloadsFlow = MutableSharedFlow<List<UIJob>>(replay = 1)
    val currentDownloads = _currentDownloadsFlow.asSharedFlow()

    private val _db = Room.databaseBuilder(
        context = context,
        klass = DownloadsDB::class.java,
        name = "downloads.db"
    )
        .fallbackToDestructiveMigration()
        .build()
        .downloadDao()

    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {

        DownloadService.start(context, MyDownloadService::class.java)


        _downloadedFilesDir.mkdirs()
        _scope.launch {
            withContext(Dispatchers.IO) {
                File(_downloadedFilesDir, NO_MEDIA).createNewFile()
            }
        }

        _scope.launch {
            settingsManager.profileIdFlow.collectLatest { _profileId = it }
        }

        _scope.launch {
            settingsManager.downloadOverMobileFlow.collectLatest {
                MyDownloadService.getDownloadManager(context).requirements = if (it)
                    Requirements(Requirements.NETWORK or Requirements.DEVICE_STORAGE_NOT_LOW)
                else
                    Requirements(Requirements.NETWORK_UNMETERED or Requirements.DEVICE_STORAGE_NOT_LOW)
            }
        }

        _scope.launch {
            PlayerStateManager.playbackEnded.collectLatest {
                val jobs = _db.getJobs(_profileId)
                for(job in jobs) {
                    job.pending = true
                    _db.update(job)
                }
            }
        }

        _statusTimer.schedule(
            delay = 500,
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

        _scope.launch {
            try {
                _statusTimerMutex.withLock {
                    statusTimerWork()
                }
            } catch (_: IllegalStateException) {
            } catch (ex: Exception) {
                Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
                ex.logToCrashlytics()
            }
        }
    }

    private suspend fun statusTimerWork() {

        if (PlayerStateManager.playerScreenVisible)
            return

        val exoIndex = MyDownloadService.getDownloadManager(context).downloadIndex

        val dbJobs = _db.getJobs(_profileId)
        val dbDownloads = _db.getDownloads(_profileId)


        val uiJobs = ArrayList<UIJob>()
        for (dbJob in dbJobs) {
            val jobArtPath = _downloadedFilesDir.listFiles()?.firstOrNull {
                it.name == dbJob.artworkFile
            }?.path ?: dbJob.artworkUrl

            val uiJob = UIJob(
                key = dbJob.id.toString(),
                mediaId = dbJob.mediaId,
                mediaType = dbJob.mediaType,
                title = dbJob.title,
                artworkUrl = jobArtPath ?: "",
                artworkIsPoster = dbJob.artworkIsPoster,
                downloads = listOf(),
                count = dbJob.count
            )
            uiJobs.add(uiJob)

            val uiDownloads = ArrayList<UIDownload>()
            for (dbDownload in dbDownloads.filter { it.jobId == dbJob.id }) {

                val downloadArtPath = _downloadedFilesDir.listFiles()?.firstOrNull {
                    it.name == dbDownload.artworkFile
                }?.path ?: dbDownload.artworkUrl

                val uiDownload = UIDownload(
                    key = "${dbJob.id}.${dbDownload.id}",
                    mediaId = dbDownload.mediaId,
                    title = dbDownload.title,
                    artworkUrl = downloadArtPath ?: "",
                    artworkIsPoster = dbDownload.artworkIsPoster,
                    played = dbDownload.played,
                    introStartTime = dbDownload.introStartTime,
                    introEndTime = dbDownload.introEndTime,
                    creditsStartTime = dbDownload.creditsStartTime
                )
                uiDownloads.add(uiDownload)

                val exoDownload = exoIndex.getDownload(dbDownload.mediaId.toString())
                if (exoDownload != null) {

                    uiDownload.percent = exoDownload.percentDownloaded / 100
                    uiDownload.status =
                        when (exoDownload.state) {
                            Download.STATE_FAILED -> DownloadStatus.Error
                            Download.STATE_QUEUED -> DownloadStatus.Pending
                            Download.STATE_STOPPED -> DownloadStatus.Paused
                            Download.STATE_REMOVING -> DownloadStatus.Paused
                            Download.STATE_COMPLETED -> DownloadStatus.Finished
                            else -> DownloadStatus.Running
                        }

                    if(exoDownload.state == Download.STATE_STOPPED) {
                        Log.d(TAG, "Download stopped: ${exoDownload.stopReason}")
                    }

                    if(exoDownload.state == Download.STATE_FAILED) {
                        Log.d(TAG, "Download failed: ${exoDownload.failureReason}")
                    }

                    if(exoDownload.state == Download.STATE_RESTARTING) {
                        Log.d(TAG, "Download restarting: ${exoDownload.failureReason}")
                    }

                }
            }

            uiJob.status = when(true) {
                dbJob.pending -> DownloadStatus.Pending
                uiDownloads.isEmpty() -> DownloadStatus.Pending
                uiDownloads.all { it.status == DownloadStatus.Finished } -> DownloadStatus.Finished
                uiDownloads.any { it.status == DownloadStatus.Error } -> DownloadStatus.Error
                else -> DownloadStatus.Running
            }

            if(uiJob.mediaType == MediaTypes.Movie || uiJob.mediaType == MediaTypes.Episode){
                if(uiDownloads.size == 0) {
                    uiDownloads.add(
                        UIDownload(
                            key = "${dbJob.id}.tmp",
                            mediaId = uiJob.mediaId,
                            title = uiJob.title,
                            artworkUrl = uiJob.artworkUrl,
                            artworkIsPoster = uiJob.artworkIsPoster,
                        )
                    )
                }
            }

            uiJob.downloads = uiDownloads
        }

        _currentDownloadsFlow.tryEmit(uiJobs.toList())
    }


    private fun updateTimerTick() {
        _scope.launch {
            try {
                _updateTimerMutex.withLock {
                    updateTimerWork()
                }
            } catch (_: IllegalStateException) {
            } catch (ex: Exception) {
                Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
                ex.logToCrashlytics()
            }
        }
    }

    private suspend fun updateTimerWork() {

        val exoDM = MyDownloadService.getDownloadManager(context)

        //Don't do anything until we know what to do
        if (_profileId <= 0) {
            if (!exoDM.downloadsPaused)
                exoDM.pauseDownloads()
            return
        }

        //No downloads without a network
        if (!networkManager.isConnected())
            return

        exoDM.resumeDownloads()
        val exoIndex = exoDM.downloadIndex


        val dbJobs = _db.getJobs(_profileId)
        val dbDownloads = ArrayList<DBDownload>(_db.getDownloads(_profileId))


        //Check for valid artwork downloads
        val validArtworkUrls = ArrayList<String>()
        val validArtworkFiles = ArrayList<String>()
        val downloadedArtwork = ArrayList<String>()
        for (dbJob in dbJobs) {

            if(dbJob.pending) {

                //Reduce download count immediately if job changed
                //This updates the UI faster
                val jobDownloads = ArrayList<DBDownload>()
                for (dbDownload in dbDownloads) {
                    if (dbDownload.jobId == dbJob.id)
                        jobDownloads.add(dbDownload)
                }

                if (dbJob.count < jobDownloads.size) {
                    for ((idx, dbDownload) in jobDownloads.withIndex()) {
                        if (idx > dbJob.count - 1) {
                            _db.delete(dbDownload)
                            dbDownloads.remove(dbDownload)
                        }
                    }
                }
            }


            //Job artwork
            if (dbJob.artworkUrl != null) {
                validArtworkUrls.add(dbJob.artworkUrl)
                validArtworkFiles.add(dbJob.artworkFile!!)
                validArtworkFiles.add(dbJob.artworkFile + ".tmp")
                if (File(_downloadedFilesDir, dbJob.artworkFile).exists())
                    downloadedArtwork.add(dbJob.artworkUrl)
            }
        }

        //Downloads artwork
        for (dbDownload in dbDownloads) {
            if (dbDownload.artworkUrl != null) {
                validArtworkUrls.add(dbDownload.artworkUrl)
                validArtworkFiles.add(dbDownload.artworkFile!!)
                validArtworkFiles.add(dbDownload.artworkFile + ".tmp")
                if (File(_downloadedFilesDir, dbDownload.artworkFile).exists())
                    downloadedArtwork.add(dbDownload.artworkUrl)
            }
        }

        //Artwork
        val admCursor = _androidDownloadManager.query(AndroidDownloadManager.Query())
        while (admCursor.moveToNext()) {
            var index = admCursor.getColumnIndex(AndroidDownloadManager.COLUMN_URI)
            val url = admCursor.getString(index)
            var valid = validArtworkUrls.contains(url)
            if (valid) {
                downloadedArtwork.add(url)
                index = admCursor.getColumnIndex(AndroidDownloadManager.COLUMN_STATUS)
                val statusCode = admCursor.getInt(index)
                if (statusCode == AndroidDownloadManager.STATUS_SUCCESSFUL) {
                    val dbDownload = dbDownloads.firstOrNull { it.artworkUrl == url }
                    if (dbDownload != null) {
                        val tmpFile = File(_downloadedFilesDir, dbDownload.artworkFile + ".tmp")
                        if (tmpFile.exists()) {
                            val finFile = File(_downloadedFilesDir, dbDownload.artworkFile!!)
                            if (finFile.exists())
                                finFile.delete()
                            tmpFile.renameTo(finFile)
                        }
                    }
                    valid = false
                }
            }
            if (!valid) {
                index = admCursor.getColumnIndex(AndroidDownloadManager.COLUMN_ID)
                val androidId = admCursor.getLong(index)
                _androidDownloadManager.remove(androidId)

            }
        }
        admCursor.close()

        //Delete invalid files
        for (filename in _downloadedFilesDir.list() ?: arrayOf()) {
            val valid = filename == NO_MEDIA || validArtworkFiles.contains(filename)
            if (!valid)
                File(_downloadedFilesDir, filename).delete()
        }


        //Download missing artwork
        for (dbJob in dbJobs) {
            downloadArtworkIfNeeded(downloadedArtwork, dbJob.artworkUrl, dbJob.artworkFile)
        }

        for (dbDownload in dbDownloads) {
            downloadArtworkIfNeeded(
                downloadedArtwork,
                dbDownload.artworkUrl,
                dbDownload.artworkFile
            )
        }


        //Check for video downloads to remove
        val cursor = exoIndex.getDownloads()
        while (cursor.moveToNext()) {
            val exoDownload = cursor.download
            val request = exoDownload.request

            //Check if this download is supposed to exist
            val dbDownload = dbDownloads.firstOrNull { it.mediaId.toString() == request.id }
            if (dbDownload == null) {

                Log.d(TAG, "Removing download: ${request.id}")

                //No longer valid: Remove
                DownloadService.sendRemoveDownload(
                    context,
                    MyDownloadService::class.java,
                    request.id,
                    false
                )
            }
        }
        cursor.close()


        //Add any missing video downloads
        for (dbDownload in dbDownloads) {
            val exoDownload = exoIndex.getDownload(dbDownload.mediaId.toString())
            if (exoDownload == null) {

                Log.d(TAG, "Adding download: ${dbDownload.mediaId}")

                val uri = dbDownload.url.toUri()

                //Only mediaId, Uri and MimeType are persisted to the DownloadRequest
                val mediaItem = MediaItem
                    .Builder()
                    .setMediaId(dbDownload.mediaId.toString())
                    .setUri(uri)
                    .setMimeType(
                        Util.getAdaptiveMimeTypeForContentType(
                            Util.inferContentType(uri)
                        )
                    )
                    .build()

                val downloadHelper = DownloadHelper.forMediaItem(
                    context,
                    mediaItem,
                    null,
                    MyDownloadService.getHttpDataSourceFactory(context)
                )

                val downloadRequest = downloadHelper.getDownloadRequest(
                    dbDownload.mediaId.toString(),
                    null
                )

                downloadHelper.release()

                DownloadService.sendAddDownload(
                    context,
                    MyDownloadService::class.java,
                    downloadRequest,
                    false
                )
            }
        }


        //Update jobs
        for (dbJob in dbJobs) {
            try {
                var pending = dbJob.pending
                if (!pending)
                    if (dbJob.mediaType == MediaTypes.Series || dbJob.mediaType == MediaTypes.Playlist)
                        pending = dbJob.lastUpdate.minutesSince() >= UPDATE_MINUTES
                if (pending) {
                    when (dbJob.mediaType) {
                        MediaTypes.Series -> updateSeries(dbJob)
                        MediaTypes.Playlist -> updatePlaylist(dbJob)
                        MediaTypes.Movie -> updateMovie(dbJob)
                        MediaTypes.Episode -> updateEpisode(dbJob)
                    }
                }
            } catch (ex: Exception) {
                Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
                ex.logToCrashlytics()
            }
        }
    }


    private fun downloadArtworkIfNeeded(
        downloadedArtwork: ArrayList<String>,
        url: String?,
        file: String?
    ) {

        if (url.isNullOrBlank())
            return

        if (file.isNullOrBlank())
            return

        if (downloadedArtwork.contains(url))
            return

        var filename = File(_downloadedFilesDir, file)
        if (filename.exists())
            return

        filename = File(_downloadedFilesDir, "$file.tmp")
        if (filename.exists())
            filename.delete()

        val uri = url.toUri()
        val request = DownloadManager.Request(uri)
        request.setDestinationUri(Uri.fromFile(filename))
        request.setAllowedOverMetered(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        _androidDownloadManager.enqueue(request)

        downloadedArtwork.add(url)
    }

    private fun getFilePrefix(mediaType: MediaTypes): String = when (mediaType) {
        MediaTypes.Movie -> "movie"
        MediaTypes.Series -> "series"
        MediaTypes.Episode -> "episode"
        MediaTypes.Playlist -> "playlist"
    }

    private fun getArtworkFilename(
        mediaId: Int,
        mediaType: MediaTypes,
        isPoster: Boolean,
        url: String?
    ): String? {

        if (url.isNullOrBlank())
            return null

        var ext = url.toUri().lastPathSegment ?: ""
        if (ext.contains('.'))
            ext = ext.substring(ext.lastIndexOf('.') + 1)

        var prefix = getFilePrefix(mediaType)
        prefix += if (isPoster) ".poster" else ".backdrop"

        return "$prefix.$mediaId.$ext"
    }


    private fun Date.secondsSince(): Long {
        val diff = Date().time - this.time
        return diff / 1000
    }

    private fun Date.minutesSince(): Long {
        return this.secondsSince() / 60
    }



    private suspend fun updateMovie(job: DBJob) {

        val detailedMovie = moviesRepository.details(id = job.mediaId)

        var dbDownload = _db.getDownload(_profileId, job.id, detailedMovie.id)
        if (dbDownload == null) {

            val artworkUrl = detailedMovie.backdropUrl ?: detailedMovie.artworkUrl
            val isPoster = artworkUrl == detailedMovie.artworkUrl

            dbDownload = DBDownload(
                jobId = job.id,
                sortIndex = 0,
                mediaId = detailedMovie.id,
                mediaType = MediaTypes.Movie,
                profileId = _profileId,
                playlistItemId = 0,
                title = detailedMovie.title,
                url = detailedMovie.videoUrl!!,
                artworkUrl = artworkUrl,
                artworkFile = getArtworkFilename(
                    mediaId = detailedMovie.id,
                    mediaType = MediaTypes.Movie,
                    isPoster = isPoster,
                    url = artworkUrl
                ),
                artworkIsPoster = isPoster,
                played = detailedMovie.played,
                introStartTime = detailedMovie.introStartTime,
                introEndTime = detailedMovie.introEndTime,
                creditsStartTime = detailedMovie.creditsStartTime
            )
            _db.insert(dbDownload)
        } else {
            dbDownload.played = detailedMovie.played
            dbDownload.introStartTime = detailedMovie.introStartTime
            dbDownload.introEndTime = detailedMovie.introEndTime
            dbDownload.creditsStartTime = detailedMovie.creditsStartTime
            _db.update(dbDownload)
        }

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }

    private suspend fun updateSeries(job: DBJob) {

        val detailedSeries = seriesRepository.details(job.mediaId)

        //Identify ids of episodes that should be downloaded
        var upNext = detailedSeries.episodes?.firstOrNull {
            it.upNext
        }
        if (upNext == null)
            upNext = detailedSeries.episodes?.first()

        val dbDownloads = ArrayList<DBDownload>(_db.getDownloads(_profileId, job.id))

        if (upNext == null) {
            for (dbDownload in dbDownloads) {
                _db.delete(dbDownload)
            }
        } else {
            var reachedUpNext = false
            var count = 0
            val itemIds = ArrayList<Int>()
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

            //Remove any episodes that should no longer be downloaded
            for (dbDownload in dbDownloads)
                if (!itemIds.contains(dbDownload.mediaId))
                    _db.delete(dbDownload)
            dbDownloads.removeAll {
                !itemIds.contains(it.mediaId)
            }

            //Add any episodes that should be downloaded
            for ((sortIndex, itemId) in itemIds.withIndex()) {
                val episode = detailedSeries.episodes.first { it.id == itemId }
                var dbDownload = dbDownloads.firstOrNull { it.mediaId == itemId }
                if (dbDownload == null) {
                    dbDownload = DBDownload(
                        jobId = job.id,
                        sortIndex = sortIndex,
                        mediaId = itemId,
                        mediaType = MediaTypes.Episode,
                        profileId = _profileId,
                        playlistItemId = 0,
                        title = episode.fullDisplayTitle(),
                        url = episode.videoUrl,
                        artworkUrl = episode.artworkUrl,
                        artworkFile = getArtworkFilename(
                            episode.id,
                            MediaTypes.Episode,
                            false,
                            episode.artworkUrl
                        ),
                        artworkIsPoster = false,
                        played = episode.played,
                        introStartTime = episode.introStartTime,
                        introEndTime = episode.introEndTime,
                        creditsStartTime = episode.creditsStartTime
                    )
                    _db.insert(dbDownload)
                } else {
                    dbDownload.played = episode.played
                    dbDownload.introStartTime = episode.introStartTime
                    dbDownload.introEndTime = episode.introEndTime
                    dbDownload.creditsStartTime = episode.creditsStartTime
                    dbDownload.sortIndex = sortIndex
                    _db.update(dbDownload)
                }
            }
        }

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }

    private suspend fun updateEpisode(job: DBJob) {

        val detailedEpisode = episodesRepository.details(id = job.mediaId)
        var dbDownload = _db.getDownload(_profileId, job.id, detailedEpisode.id)
        if (dbDownload == null) {

            dbDownload = DBDownload(
                jobId = job.id,
                sortIndex = 0,
                mediaId = detailedEpisode.id,
                mediaType = MediaTypes.Episode,
                profileId = _profileId,
                playlistItemId = 0,
                title = detailedEpisode.fullDisplayTitle(),
                url = detailedEpisode.videoUrl,
                artworkUrl = detailedEpisode.artworkUrl,
                artworkFile = getArtworkFilename(
                    mediaId = detailedEpisode.id,
                    mediaType = MediaTypes.Episode,
                    isPoster = false,
                    url = detailedEpisode.artworkUrl
                ),
                artworkIsPoster = false,
                played = detailedEpisode.played,
                introStartTime = detailedEpisode.introStartTime,
                introEndTime = detailedEpisode.introEndTime,
                creditsStartTime = detailedEpisode.creditsStartTime
            )
            _db.insert(dbDownload)
        } else {
            dbDownload.played = detailedEpisode.played
            dbDownload.introStartTime = detailedEpisode.introStartTime
            dbDownload.introEndTime = detailedEpisode.introEndTime
            dbDownload.creditsStartTime = detailedEpisode.creditsStartTime
            _db.update(dbDownload)
        }

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }

    private suspend fun updatePlaylist(job: DBJob) {

        val detailedPlaylist = playlistRepository.details(job.mediaId)

        //Identify ids of items that should be downloaded
        var upNext = detailedPlaylist.items?.firstOrNull {
            it.index == detailedPlaylist.currentItemId
        }
        if (upNext == null)
            upNext = detailedPlaylist.items?.first()

        val dbDownloads = ArrayList(_db.getDownloads(_profileId, job.id))

        if (upNext == null) {
            for (dbDownload in dbDownloads) {
                _db.delete(dbDownload)
            }
        } else {

            var reachedUpNext = false
            var count = 0
            val itemIds = ArrayList<Int>()
            val mediaIds = ArrayList<Int>()
            for (playListItem in detailedPlaylist.items!!) {
                if (upNext.id == playListItem.id)
                    reachedUpNext = true
                if (reachedUpNext) {
                    itemIds.add(playListItem.id)
                    if (!mediaIds.contains(playListItem.mediaId)) {
                        mediaIds.add(playListItem.mediaId)
                        count++
                        if (count >= job.count)
                            break
                    }
                }
            }

            //Remove any items that should no longer be downloaded
            for (dbDownload in dbDownloads)
                if (!itemIds.contains(dbDownload.playlistItemId))
                    _db.delete(dbDownload)
            dbDownloads.removeAll {
                !itemIds.contains(it.playlistItemId)
            }

            //Add any items that should be downloaded
            for (playlistItemId in itemIds) {
                val playlistItem = detailedPlaylist.items!!.first { it.id == playlistItemId }
                var dbDownload = dbDownloads.firstOrNull { it.playlistItemId == playlistItemId }
                if (dbDownload == null) {

                    val artworkUrl = playlistItem.backdropUrl ?: playlistItem.artworkUrl
                    val isPoster = artworkUrl == playlistItem.artworkUrl

                    dbDownload = DBDownload(
                        jobId = job.id,
                        sortIndex = playlistItem.index,
                        mediaId = playlistItem.mediaId,
                        mediaType = playlistItem.mediaType,
                        profileId = _profileId,
                        playlistItemId = playlistItem.id,
                        title = playlistItem.title,
                        url = playlistItem.videoUrl,
                        artworkUrl = playlistItem.artworkUrl,
                        artworkFile = getArtworkFilename(
                            playlistItem.mediaId,
                            playlistItem.mediaType,
                            isPoster,
                            playlistItem.artworkUrl
                        ),
                        artworkIsPoster = isPoster,
                        played =
                            if(playlistItemId == detailedPlaylist.currentItemId)
                                detailedPlaylist.currentProgress
                            else
                                0.0,
                        introStartTime = playlistItem.introStartTime,
                        introEndTime = playlistItem.introEndTime,
                        creditsStartTime = playlistItem.creditsStartTime
                    )
                    _db.insert(dbDownload)
                } else  {

                    dbDownload.played =
                        if(playlistItemId == detailedPlaylist.currentItemId)
                            detailedPlaylist.currentProgress
                        else
                            0.0
                    dbDownload.introStartTime = playlistItem.introStartTime
                    dbDownload.introEndTime = playlistItem.introEndTime
                    dbDownload.creditsStartTime = playlistItem.creditsStartTime
                    dbDownload.sortIndex = playlistItem.index
                    _db.update(dbDownload)
                }
            }
        }

        job.pending = false
        job.lastUpdate = Date()
        _db.update(job)
    }


    suspend fun addMovie(detailedMovie: DetailedMovie) {

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val artworkUrl = detailedMovie.backdropUrl ?: detailedMovie.artworkUrl
        val isPoster = artworkUrl == detailedMovie.artworkUrl

        val job = DBJob(
            mediaId = detailedMovie.id,
            mediaType = MediaTypes.Movie,
            profileId = _profileId,
            title = detailedMovie.displayTitle(),
            count = 1,
            artworkUrl = artworkUrl,
            artworkFile = getArtworkFilename(
                mediaId = detailedMovie.id,
                mediaType = MediaTypes.Movie,
                isPoster = isPoster,
                url = artworkUrl
            ),
            artworkIsPoster = isPoster,
            lastUpdate = lastUpdate.time
        )

        _db.insert(job)
    }

    suspend fun addOrUpdateSeries(detailedSeries: DetailedSeries, count: Int) {

        if (count == 0) {
            delete(detailedSeries.id, MediaTypes.Series)
            return
        }

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        var job = _db.getJob(detailedSeries.id, MediaTypes.Series, _profileId)
        if (job == null) {

            val artworkUrl = detailedSeries.backdropUrl ?: detailedSeries.artworkUrl
            val isPoster = artworkUrl == detailedSeries.artworkUrl

            job = DBJob(
                mediaId = detailedSeries.id,
                mediaType = MediaTypes.Series,
                profileId = _profileId,
                title = detailedSeries.title,
                count = count,
                artworkUrl = artworkUrl,
                artworkFile = getArtworkFilename(
                    mediaId = detailedSeries.id,
                    mediaType = MediaTypes.Series,
                    isPoster = isPoster,
                    url = artworkUrl
                ),
                artworkIsPoster = isPoster,
                lastUpdate = lastUpdate.time
            )

            _db.insert(job)

        } else if (job.count != count) {
            job.count = count
            job.lastUpdate = lastUpdate.time
            job.pending = true
            _db.update(job)
        }

    }

    suspend fun updateSeries(mediaId: Int, newCount: Int) {

        if (newCount == 0) {
            delete(mediaId, MediaTypes.Series)
            return
        }

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = _db.getJob(mediaId, MediaTypes.Series, _profileId)
        if (job != null) {
            if (job.count != newCount) {
                job.count = newCount
                job.lastUpdate = lastUpdate.time
                job.pending = true
                _db.update(job)
            }
        }
    }

    suspend fun addEpisode(detailedEpisode: DetailedEpisode) {

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = DBJob(
            mediaId = detailedEpisode.id,
            mediaType = MediaTypes.Episode,
            profileId = _profileId,
            title = "S${detailedEpisode.seasonNumber}:${detailedEpisode.episodeNumber}: ${detailedEpisode.title}",
            count = 1,
            lastUpdate = lastUpdate.time,
            artworkUrl = detailedEpisode.artworkUrl,
            artworkFile = getArtworkFilename(
                mediaId = detailedEpisode.id,
                mediaType = MediaTypes.Episode,
                isPoster = false,
                url = detailedEpisode.artworkUrl
            ),
            artworkIsPoster = false
        )

        _db.insert(job)
    }

    suspend fun addOrUpdatePlaylist(detailedPlaylist: DetailedPlaylist, count: Int) {

        if (count == 0) {
            delete(detailedPlaylist.id, MediaTypes.Playlist)
            return
        }

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        var job = _db.getJob(detailedPlaylist.id, MediaTypes.Playlist, _profileId)
        if (job == null) {

            val artworkUrl = detailedPlaylist.backdropUrl

            job = DBJob(
                mediaId = detailedPlaylist.id,
                mediaType = MediaTypes.Playlist,
                profileId = _profileId,
                title = detailedPlaylist.name,
                count = count,
                artworkUrl = artworkUrl,
                artworkFile = getArtworkFilename(
                    detailedPlaylist.id,
                    MediaTypes.Playlist,
                    false,
                    artworkUrl
                ),
                artworkIsPoster = false,
                lastUpdate = lastUpdate.time
            )
            _db.insert(job)
        } else if (job.count != count) {
            job.count = count
            job.lastUpdate = lastUpdate.time
            job.pending = true
            _db.update(job)
        }
    }

    suspend fun updatePlaylist(mediaId: Int, newCount: Int) {

        if (newCount == 0) {
            delete(mediaId, MediaTypes.Playlist)
            return
        }

        val lastUpdate = Calendar.getInstance()
        lastUpdate.add(Calendar.MINUTE, -2 * UPDATE_MINUTES)

        val job = _db.getJob(mediaId, MediaTypes.Playlist, _profileId)
        if (job != null) {
            if (job.count != newCount) {
                job.count = newCount
                job.lastUpdate = lastUpdate.time
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




    private fun buildMediaItem(request: DownloadRequest, dbDownload: DBDownload): MediaItem {

        var artworkUri = dbDownload.artworkUrl?.toUri()
        if(dbDownload.artworkFile != null) {
            val file = File(_downloadedFilesDir, dbDownload.artworkFile)
            if(file.exists())
                artworkUri = file.toUri()
        }

        val builder = request
            .toMediaItem()
            .buildUpon()
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setArtworkUri(artworkUri)
                    .setMediaType(
                        when(dbDownload.mediaType) {
                            MediaTypes.Movie -> MediaMetadata.MEDIA_TYPE_MOVIE
                            MediaTypes.Episode -> MediaMetadata.MEDIA_TYPE_TV_SERIES
                            else -> MediaMetadata.MEDIA_TYPE_VIDEO
                        }
                    )
                    .setTitle(dbDownload.title)
                    .build()
            )
            .setTag(dbDownload)

        return builder.build()
    }


    suspend fun getMediaItem(mediaId: Int): MediaItem? {

        val dbDownloads = _db.getDownloads(_profileId)
        val dbDownload = dbDownloads.firstOrNull {
            it.mediaId == mediaId
        } ?: return null

        val exoDM = MyDownloadService.getDownloadManager(context)
        val download = exoDM.downloadIndex.getDownload(mediaId.toString()) ?: return null

        return buildMediaItem(download.request, dbDownload)
    }

    suspend fun getAllDownloadsInfo(): List<DownloadPlaybackInfo> {

        val ret = ArrayList<DownloadPlaybackInfo>()

        val exoDM = MyDownloadService.getDownloadManager(context)
        val exoIndex = exoDM.downloadIndex
        val dbJobs = _db.getJobs(_profileId)
        val dbDownloads = _db.getDownloads(_profileId)

        for(dbJob in dbJobs) {
            for(dbDownload in dbDownloads) {
                if(dbDownload.jobId == dbJob.id) {
                    val download = exoIndex.getDownload(dbDownload.mediaId.toString()) ?: continue
                    val mediaItem = buildMediaItem(download.request, dbDownload)
                    ret.add(DownloadPlaybackInfo(dbJob, dbDownload, mediaItem))
                }
            }
        }

        return ret
    }
}















