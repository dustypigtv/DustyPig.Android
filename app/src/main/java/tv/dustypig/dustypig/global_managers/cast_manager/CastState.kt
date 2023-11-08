package tv.dustypig.dustypig.global_managers.cast_manager

import androidx.mediarouter.media.MediaRouter.RouteInfo

data class CastState(
    val availableRoutes: List<RouteInfo> = listOf(),
    val selectedRoute: RouteInfo? = null,
    val title: String? = null,
    val artworkUrl: String? = null,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
    val playbackStatus: CastPlaybackStatus = CastPlaybackStatus.Stopped,
    val position: Long = 0L,
    val duration: Long = 0L,
    val progress: Float = 0F
)
