package tv.dustypig.dustypig.global_managers.cast_manager

import androidx.mediarouter.media.MediaRouter.RouteInfo

data class CastState(
    val castAvailable: Boolean = false,
    val connected: Boolean = false,
    val playingContent: Boolean = false,
    val availableRoutes: List<RouteInfo> = listOf(),
    val selectedRoute: RouteInfo,
    val defaultRoute: RouteInfo,
    val hasInfo: Boolean = false,
    val title: String? = null,
    val artworkUrl: String? = null,
    val position: Float = 0.0f,
    val duration: Float = 0.0f,
    val progress: Float = 0.0f,
    val paused: Boolean = false,
    val buffering: Boolean = false,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false
)
