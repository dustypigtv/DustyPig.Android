package tv.dustypig.dustypig.global_managers.download_manager

data class UIDownload(
    val key: String,
    val mediaId: Int,
    val title: String,
    val artworkUrl: String = "",
    val artworkIsPoster: Boolean = false,
    var percent: Float = 0f,
    var status: DownloadStatus = DownloadStatus.Pending,
    var statusDetails: String = "",
    val played: Double? = null,
    val introStartTime: Double? = null,
    val introEndTime: Double? = null,
    val creditsStartTime: Double? = null
)