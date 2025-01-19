package tv.dustypig.dustypig.global_managers.media_cache_manager

import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicTMDB
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.DetailedSeries
import java.util.UUID

object MediaCacheManager {

    val BasicInfo: ArrayList<CachedBasicInfo> = arrayListOf()
    val Movies: MutableMap<String, DetailedMovie> = mutableMapOf()
    val Series: MutableMap<String, DetailedSeries> = mutableMapOf()
    val Playlists: MutableMap<String, DetailedPlaylist> = mutableMapOf()

    private fun add(cachedBasicInfo: CachedBasicInfo): String {
        BasicInfo.add(cachedBasicInfo)
        return cachedBasicInfo.cacheId
    }

    fun add(basicMedia: BasicMedia): String {
        return add(
            CachedBasicInfo(
                title = basicMedia.title,
                posterUrl = basicMedia.artworkUrl,
                backdropUrl = basicMedia.backdropUrl
            )
        )
    }

    fun add(basicTMDB: BasicTMDB): String {
        return add(
            CachedBasicInfo(
                title = basicTMDB.title,
                posterUrl = basicTMDB.artworkUrl ?: "",
                backdropUrl = basicTMDB.backdropUrl
            )
        )
    }

    fun add(title: String, posterUrl: String, backdropUrl: String?): String {
        return add(
            CachedBasicInfo(
                title = title,
                posterUrl = posterUrl,
                backdropUrl = backdropUrl
            )
        )
    }

    fun getBasicInfo(cacheId: String) = BasicInfo.firstOrNull {
        it.cacheId == cacheId
    } ?: CachedBasicInfo()

    fun add(detailedMovie: DetailedMovie): String {
        val id = UUID.randomUUID().toString()
        Movies[id] = detailedMovie
        return id
    }

    fun add(detailedSeries: DetailedSeries): String {
        val id = UUID.randomUUID().toString()
        Series[id] = detailedSeries
        return id
    }


    fun add(detailedPlaylist: DetailedPlaylist): String {
        val id = UUID.randomUUID().toString()
        Playlists[id] = detailedPlaylist
        return id
    }

    fun reset() {
        BasicInfo.clear()
        Movies.clear()
        Series.clear()
        Playlists.clear()
    }
}