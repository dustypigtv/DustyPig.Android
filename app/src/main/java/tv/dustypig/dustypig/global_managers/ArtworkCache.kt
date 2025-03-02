package tv.dustypig.dustypig.global_managers

import kotlinx.coroutines.sync.Mutex
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicPerson
import tv.dustypig.dustypig.api.models.BasicPlaylist
import tv.dustypig.dustypig.api.models.BasicTMDB
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.PlaylistItem
import tv.dustypig.dustypig.api.models.TMDBMediaTypes

/**
 * This does NOT replace things like coil.
 * This holds urls for media ids in cache for smother screen transitions
 * Art will be added to the cache before loading the view model that needs it
 * Then the view model will release it when done.
 */
object ArtworkCache {

    private data class CachedEntry(
        var count: Int,
        val url: String
    )


    private val mediaPosters = HashMap<Int, CachedEntry>()
    private val mediaBackdrops = HashMap<Int, CachedEntry>()
    private val playlistPosters = HashMap<Int, CachedEntry>()
    private val playlistBackdrops = HashMap<Int, CachedEntry>()
    private val tmdbMoviePosters = HashMap<Int, CachedEntry>()
    private val tmdbMovieBackdrops = HashMap<Int, CachedEntry>()
    private val tmdbSeriesPosters = HashMap<Int, CachedEntry>()
    private val tmdbSeriesBackdrops = HashMap<Int, CachedEntry>()
    private val tmdbPeople = HashMap<Int, CachedEntry>()

    private val mutex = Mutex(locked = false)


    private inline fun <T> Mutex.myLock (action: () -> T): T? {
        if (this.tryLock()) {
            try {
                return action()
            } catch(_: Exception) {
            } finally {
                this.unlock()
            }
        }
        return null
    }

    private fun add(
        id: Int,
        poster: String?,
        posterCache: HashMap<Int, CachedEntry>,
        backdrop: String?,
        backdropCache: HashMap<Int, CachedEntry>
    ) {
        mutex.myLock {
            if(!poster.isNullOrBlank()) {
                var ce = posterCache[id]
                if (ce == null) {
                    ce = CachedEntry(0, poster)
                    posterCache[id] = ce
                }
                ce.count++
            }

            if(!backdrop.isNullOrBlank()) {
                var ce = backdropCache[id]
                if (ce == null) {
                    ce = CachedEntry(0, backdrop)
                    backdropCache[id] = ce
                }
                ce.count++
            }
        }
    }


    private fun get(id: Int, cache: HashMap<Int, CachedEntry>): String? {
        return mutex.myLock {
            val ce = cache[id]
            ce?.url
        }
    }


    private fun delete(id: Int, cache: HashMap<Int, CachedEntry>) {
        mutex.myLock {
            val ce = cache[id]
            if (ce != null) {
                ce.count--
                if (ce.count <= 0) {
                    cache.remove(id)
                }
            }
        }
    }




    fun add(basicMedia: BasicMedia) = add(
        basicMedia.id,
        basicMedia.artworkUrl,
        mediaPosters,
        basicMedia.backdropUrl,
        mediaBackdrops
    )


    fun getMediaPoster(id: Int) = get(id, mediaPosters)

    fun getMediaBackdrop(id: Int) = get(id, mediaBackdrops)

    fun deleteMedia(id: Int) {
        delete(id, mediaPosters)
        delete(id, mediaBackdrops)
    }




    fun add(detailedEpisode: DetailedEpisode) {
        mutex.myLock {
            if (detailedEpisode.artworkUrl.isNotBlank()) {
                var ce = mediaBackdrops[detailedEpisode.id]
                if (ce == null) {
                    ce = CachedEntry(0, detailedEpisode.artworkUrl)
                    mediaBackdrops[detailedEpisode.id] = ce
                }
                ce.count++
            }
        }
    }




    fun add(basicPlaylist: BasicPlaylist) = add(
        basicPlaylist.id,
        basicPlaylist.artworkUrl,
        playlistPosters,
        basicPlaylist.backdropUrl,
        playlistBackdrops
    )

    fun addPlaylist(id: Int, posterUrl: String?, backdropUrl: String?) = add(
        id,
        posterUrl,
        playlistPosters,
        backdropUrl,
        playlistBackdrops
    )

    fun add(playlistItem: PlaylistItem) = add(
        playlistItem.mediaId,
        playlistItem.artworkUrl,
        mediaPosters,
        playlistItem.backdropUrl,
        mediaBackdrops
    )

    fun deletePlaylist(id: Int) {
        delete(id, playlistPosters)
        delete(id, playlistBackdrops)
    }

    fun getPlaylistPoster(id: Int) = get(id, playlistPosters) ?: BasicPlaylist.DEFAULT_ARTWORK

    fun getPlaylistBackdrop(id: Int) = get(id, playlistBackdrops) ?: BasicPlaylist.DEFAULT_BACKDROP




    fun add(basicTMDB: BasicTMDB) {
        if(basicTMDB.mediaType == TMDBMediaTypes.Movie) {
            add(
                basicTMDB.tmdbId,
                basicTMDB.artworkUrl,
                tmdbMoviePosters,
                basicTMDB.backdropUrl,
                tmdbMovieBackdrops
            )
        } else {
            add(
                basicTMDB.tmdbId,
                basicTMDB.artworkUrl,
                tmdbSeriesPosters,
                basicTMDB.backdropUrl,
                tmdbSeriesBackdrops
            )
        }
    }

    fun getTMDBMoviePoster(id: Int) = get(id, tmdbMoviePosters)

    fun getTMDBMovieBackdrop(id: Int) = get(id, tmdbMovieBackdrops)

    fun getTMDBSeriesPoster(id: Int) = get(id, tmdbSeriesPosters)

    fun getTMDBSeriesBackdrop(id: Int) = get(id, tmdbSeriesBackdrops)

    fun deleteTMDBMovie(id: Int) {
        delete(id, tmdbMoviePosters)
        delete(id, tmdbMovieBackdrops)
    }

    fun deleteTMDBSeries(id: Int) {
        delete(id, tmdbSeriesPosters)
        delete(id, tmdbSeriesBackdrops)
    }




    fun add(basicPerson: BasicPerson) {
        mutex.myLock {
            if(!basicPerson.avatarUrl.isNullOrBlank()) {
                var ce = tmdbPeople[basicPerson.tmdbId]
                if (ce == null) {
                    ce = CachedEntry(0, basicPerson.avatarUrl)
                    tmdbPeople[basicPerson.tmdbId] = ce
                }
                ce.count++
            }
        }
    }

    fun getPersonAvatar(id: Int) = get(id, tmdbPeople) ?: BasicPerson.DEFAULT_AVATAR

    fun deletePersonAvatar(id: Int) = delete(id, tmdbPeople)

}


























