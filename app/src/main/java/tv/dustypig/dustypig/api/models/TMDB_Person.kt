package tv.dustypig.dustypig.api.models

import java.util.Date

data class TMDB_Person(
    val tmdbId: Int,
    val name: String? = null,
    val avatarUrl: String? = null,
    val birthday: Date? = null,
    val deathday: Date? = null,
    val placeOfBirth: String? = null,
    val biography: String? = null,
    val knownFor: String? = null,
    val available: List<BasicMedia> = listOf(),
    val otherTitles: List<BasicTMDB> = listOf()
)


