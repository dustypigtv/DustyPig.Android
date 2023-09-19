package tv.dustypig.dustypig.global_managers.download_manager

import tv.dustypig.dustypig.api.models.MediaTypes

data class UIJob (
    val mediaId: Int,
    val count: Int,
    val mediaType: MediaTypes,
    val title: String,
    val artworkUrl: String,
    val artworkPoster: Boolean,
    var percent: Float,
    var status: DownloadStatus,
    var statusDetails: String,
    var downloads: List<UIDownload>
)