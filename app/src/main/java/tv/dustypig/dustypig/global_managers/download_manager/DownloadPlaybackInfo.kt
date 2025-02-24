package tv.dustypig.dustypig.global_managers.download_manager

import androidx.media3.common.MediaItem

data class DownloadPlaybackInfo(
    val dbJob: DBJob,
    val dbDownload: DBDownload,
    val mediaItem: MediaItem
)
