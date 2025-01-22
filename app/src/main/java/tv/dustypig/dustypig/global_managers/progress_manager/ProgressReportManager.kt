package tv.dustypig.dustypig.global_managers.progress_manager

import android.content.Context
import android.util.Log
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.api.repositories.PlaylistRepository
import tv.dustypig.dustypig.global_managers.NetworkManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.Timer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.schedule

@Singleton
class ProgressReportManager  @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager,
    private val networkManager: NetworkManager,
    private val mediaRepository: MediaRepository,
    private val playlistRepository: PlaylistRepository
) {

    companion object {
        private const val TAG = "ProgressReportManager"

        fun getTimestamp(): String {
            val tz = TimeZone.getTimeZone("UTC")
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = tz
            return sdf.format(Calendar.getInstance(tz).time)
        }

    }

    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var _profileId: Int = -1
    private val _timer = Timer()
    private val _timerMutex = Mutex(locked = false)

    private val _db = Room.databaseBuilder(
        context = context,
        klass = ProgressDB::class.java,
        name = "progress.db"
    )
        .fallbackToDestructiveMigration()
        .build()
        .progressDao()


    init {
        _scope.launch {
            settingsManager.profileIdFlow.collectLatest {
                _profileId = it
            }
        }

        _timer.schedule(1000, 1000) {
            _scope.launch {
                try {
                    _timerMutex.withLock {
                        timerTick()
                    }
                } catch (_: IllegalStateException) {
                } catch (ex: Exception) {
                    Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
                    ex.logToCrashlytics()
                }
            }
        }
    }

    private suspend fun timerTick() {

        if (!networkManager.isConnected())
            return

        val progresses = _db.getAll()
        for (progress in progresses) {
            if (progress.profileId == _profileId) {
                try {
                    sendUpdate(progress.mediaId, progress.seconds, progress.timestamp, progress.playlist)
                    _db.delete(progress)
                } catch (ex: Exception) {
                    Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
                }
            } else {
                _db.delete(progress)
            }
        }
    }

    private suspend fun sendUpdate(mediaId: Int, seconds: Double, timestamp: String, playlist: Boolean) {
        val pp = PlaybackProgress(
            id = mediaId,
            seconds = seconds,
            asOfUTC = timestamp
        )

        if (playlist)
            mediaRepository.updatePlaybackProgress(pp)
        else
            playlistRepository.setPlaylistProgress(pp)
    }

    suspend fun setProgress(mediaId: Int, playlist: Boolean, seconds: Double) {
        try {
            var progress = _db.get(mediaId, playlist, _profileId)
            if (progress == null) {
                try {
                    if(!networkManager.isConnected())
                        throw Exception("Not connected")
                    sendUpdate(mediaId, seconds, getTimestamp(), playlist)
                } catch (_: Exception) {
                    progress = ProgressEntity(
                        mediaId = mediaId,
                        playlist = playlist,
                        profileId = _profileId,
                        seconds = seconds,
                        timestamp = getTimestamp()
                    )
                    _db.insert(progress)
                }
            } else {
                try {
                    if(!networkManager.isConnected())
                        throw Exception("Not connected")
                    sendUpdate(mediaId, seconds, getTimestamp(), playlist)
                    _db.delete(progress)
                } catch (_: Exception) {
                    progress.seconds = seconds
                    progress.timestamp = getTimestamp()
                    _db.update(progress)
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage ?: "Unknown Error", ex)
        }
    }
}