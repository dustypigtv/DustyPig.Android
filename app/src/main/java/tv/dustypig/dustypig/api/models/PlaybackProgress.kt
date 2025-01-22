package tv.dustypig.dustypig.api.models

import tv.dustypig.dustypig.global_managers.progress_manager.ProgressReportManager


data class PlaybackProgress(
    val id: Int,
    val seconds: Double,
    val asOfUTC: String = ProgressReportManager.getTimestamp()
)