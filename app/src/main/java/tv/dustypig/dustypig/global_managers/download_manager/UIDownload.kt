package tv.dustypig.dustypig.global_managers.download_manager

data class UIDownload(
    val key: String,
    val mediaId: Int,
    val title: String,
    val artworkUrl: String,
    val artworkPoster: Boolean,
    val percent: Float,
    val status: DownloadStatus,
    val statusDetails: String
)