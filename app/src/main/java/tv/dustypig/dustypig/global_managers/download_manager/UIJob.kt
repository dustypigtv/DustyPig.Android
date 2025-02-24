package tv.dustypig.dustypig.global_managers.download_manager

import tv.dustypig.dustypig.api.models.MediaTypes

data class UIJob(
    val key: String,
    val mediaId: Int,
    val count: Int,
    val mediaType: MediaTypes,
    val title: String,
    val artworkUrl: String,
    val artworkIsPoster: Boolean,
    var status: DownloadStatus = DownloadStatus.Pending,
    var downloads: List<UIDownload>
)