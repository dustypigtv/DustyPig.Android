package tv.dustypig.dustypig.download_manager

enum class DownloadStatus {
    None,
    Pending,
    Running,
    Paused,
    Error,
    Finished
}