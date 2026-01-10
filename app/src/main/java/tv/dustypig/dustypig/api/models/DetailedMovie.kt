package tv.dustypig.dustypig.api.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class DetailedMovie(
    val id: Int = 0,
    val title: String = "",
    val description: String? = null,
    val artworkUrl: String = "",
    val backdropUrl: String? = null,
    val credits: List<BasicPerson>? = null,
    val owner: String? = null,
    var played: Double? = null,
    val rated: MovieRatings = MovieRatings.None,
    val genres: Long = 0,
    val bifUrl: String? = null,
    val videoUrl: String? = null,
    val inWatchlist: Boolean = false,
    val canPlay: Boolean = false,
    val canManage: Boolean = false,
    val date: Date = Date(),
    val length: Double = 0.0,
    val introStartTime: Double? = null,
    val introEndTime: Double? = null,
    val creditsStartTime: Double? = null,
    val titleRequestPermission: TitleRequestPermissions = TitleRequestPermissions.Disabled,
    val accessRequestedStatus: OverrideRequestStatus = OverrideRequestStatus.NotRequested,
) {
    fun displayTitle(): String {
        val year = SimpleDateFormat("yyyy", Locale.US).format(date)
        return "$title ($year)"
    }
}
