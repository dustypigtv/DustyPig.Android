package tv.dustypig.dustypig.global_managers.download_manager

import android.app.Notification
import android.content.Context
import android.net.http.HttpEngine
import android.os.Build
import android.os.ext.SdkExtensions
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpEngineDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import tv.dustypig.dustypig.R
import java.io.File
import java.util.concurrent.Executors


@UnstableApi
class MyDownloadService : DownloadService(
    /* foregroundNotificationId = */ FOREGROUND_NOTIFICATION_ID_NONE,
    /* foregroundNotificationUpdateInterval = */ DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    /* channelId = */ DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    /* channelNameResourceId = */ R.string.exo_download_notification_channel_name,
    /* channelDescriptionResourceId = */ R.string.exo_download_notification_channel_description
) {

    companion object {
        private const val JOB_ID: Int = 123

        const val DOWNLOAD_NOTIFICATION_CHANNEL_ID: String = "download_channel"
        private const val DOWNLOAD_VIDEOS_DIRECTORY: String = "video_downloads"
        private const val DOWNLOAD_FILES_DIRECTORY: String = "artwork_downloads"


        private var _databaseProvider: StandaloneDatabaseProvider? = null
        private var _downloadDirectory: File? = null
        private var _downloadCache: SimpleCache? = null
        private var _httpDataSourceFactory: DataSource.Factory? = null
        private var _downloadManager: DownloadManager? = null
//        private var _downloadNotificationHelper: DownloadNotificationHelper? = null
        private var _dataSourceFactory: DataSource.Factory? = null


        @Synchronized
        fun getDownloadManager(context: Context): DownloadManager {
            if(_downloadManager == null) {
                _downloadManager = DownloadManager(
                    context,
                    getDatabaseProvider(context),
                    getDownloadCache(context),
                    getHttpDataSourceFactory(context),
                    Executors.newFixedThreadPool(6)
                )
            }
            return _downloadManager as DownloadManager
        }

        @Synchronized
        private fun getDatabaseProvider(context: Context): StandaloneDatabaseProvider {
            if (_databaseProvider == null)
                _databaseProvider = StandaloneDatabaseProvider(context)
            return _databaseProvider as StandaloneDatabaseProvider
        }

        @Synchronized
        fun getDownloadDirectory(context: Context): File {
            if (_downloadDirectory == null)
                _downloadDirectory = context.getExternalFilesDir(null) ?: context.filesDir
            return _downloadDirectory as File
        }

        @Synchronized
        private fun getDownloadVideosDirectory(context: Context): File {
            return File(getDownloadDirectory(context), DOWNLOAD_VIDEOS_DIRECTORY)
        }

        @Synchronized
        fun getDownloadedFilesDirectory(context: Context): File {
            return File(getDownloadDirectory(context), DOWNLOAD_FILES_DIRECTORY)
        }


        @Synchronized
        private fun getDownloadCache(context: Context): SimpleCache {
            if (_downloadCache == null)
                _downloadCache = SimpleCache(
                    getDownloadVideosDirectory(context),
                    NoOpCacheEvictor(),
                    getDatabaseProvider(context)
                )
            return _downloadCache as SimpleCache
        }

//    @Synchronized
//    fun getDownloadNotificationHelper(context: Context): DownloadNotificationHelper {
//        if(_downloadNotificationHelper == null) {
//            _downloadNotificationHelper = DownloadNotificationHelper(
//                context,
//                DOWNLOAD_NOTIFICATION_CHANNEL_ID
//            )
//        }
//        return _downloadNotificationHelper as DownloadNotificationHelper
//    }


        @Synchronized
        fun getHttpDataSourceFactory(context: Context): DataSource.Factory {
            if(_httpDataSourceFactory == null) {
                if (Build.VERSION.SDK_INT >= 30
                    && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.S) >= 7
                ) {
                    val httpEngine = HttpEngine.Builder(context.applicationContext).build()
                    _httpDataSourceFactory =  HttpEngineDataSource.Factory(
                        httpEngine,
                        Executors.newSingleThreadExecutor()
                    )
                } else {
                    _httpDataSourceFactory = DefaultHttpDataSource.Factory()
                }
            }
            return _httpDataSourceFactory as DataSource.Factory
        }

        @Synchronized
        fun getDataSourceFactory(context: Context): DataSource.Factory {
            if(_dataSourceFactory == null) {
                val upstreamFactory = DefaultDataSource.Factory(
                        context.applicationContext,
                        getHttpDataSourceFactory(context.applicationContext)
                )

                _dataSourceFactory = CacheDataSource.Factory()
                    .setCache(getDownloadCache(context.applicationContext))
                    .setUpstreamDataSourceFactory(upstreamFactory)
                    .setCacheWriteDataSinkFactory(null)
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            }
            return _dataSourceFactory as DataSource.Factory
        }

    }

    override fun getDownloadManager(): DownloadManager = getDownloadManager(this)

    override fun getScheduler(): Scheduler = PlatformScheduler(this, JOB_ID)

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
//        return DMUtils.getDownloadNotificationHelper(this)
//            .buildProgressNotification(
//                this,
//                R.drawable.ic_notification,
//                null,
//                null,
//                downloads,
//                notMetRequirements
//            )
        throw UnsupportedOperationException()
    }
}