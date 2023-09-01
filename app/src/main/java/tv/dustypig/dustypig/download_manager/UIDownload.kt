package tv.dustypig.dustypig.download_manager

data class UIDownload (
    val title: String,
    val percent: Float,
    val status: DownloadStatus,
    val statusDetails: String
)