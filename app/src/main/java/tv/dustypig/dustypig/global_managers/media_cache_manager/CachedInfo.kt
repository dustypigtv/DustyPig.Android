package tv.dustypig.dustypig.global_managers.media_cache_manager

import java.util.UUID

data class CachedInfo(
    val cacheId: String = UUID.randomUUID().toString(),
    val title: String = "",
    val posterUrl: String = "",
    val backdropUrl: String? = null
)
