package tv.dustypig.dustypig.global_managers.media_cache_manager

import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicTMDB

object MediaCacheManager {

    private val _cachedInfoArray: ArrayList<CachedInfo> = arrayListOf()

    fun add(cachedInfo: CachedInfo): String {
        _cachedInfoArray.add(cachedInfo)
        return cachedInfo.cacheId
    }

    fun add(basicMedia: BasicMedia): String {
        return add(
            CachedInfo(
                title = basicMedia.title,
                posterUrl = basicMedia.artworkUrl,
                backdropUrl = basicMedia.backdropUrl
            )
        )
    }

    fun add(basicTMDB: BasicTMDB): String {
        return add(
            CachedInfo(
                title = basicTMDB.title,
                posterUrl = basicTMDB.artworkUrl ?: "",
                backdropUrl = basicTMDB.backdropUrl
            )
        )
    }

    fun add(title: String, posterUrl: String, backdropUrl: String?): String {
        return add(
            CachedInfo(
                title = title,
                posterUrl = posterUrl,
                backdropUrl = backdropUrl
            )
        )
    }

    fun remove(cacheId: String) {
        _cachedInfoArray.removeAll {
            it.cacheId == cacheId
        }
    }

    fun get(cacheId: String): CachedInfo {
        return _cachedInfoArray.firstOrNull {
            it.cacheId == cacheId
        } ?: CachedInfo(
            cacheId = cacheId
        )
    }

    fun reset() {
        _cachedInfoArray.clear()
    }
}