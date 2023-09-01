package tv.dustypig.dustypig.download_manager

import tv.dustypig.dustypig.api.models.MediaTypes

data class UIJob (
    val id: Int,
    val mediaType: MediaTypes,
    val mediaId: Int,
    val title: String,
    var percent: Float,
    var status: DownloadStatus,
    var statusDetails: String,
    val downloads: List<Download>
)