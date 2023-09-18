package tv.dustypig.dustypig.global_managers.download_manager

enum class DownloadStatus {
    None,
    Pending,
    Running,
    Paused,
    Error,
    Finished
}