package tv.dustypig.dustypig.ui.main_app.screens.tmdb_details

import tv.dustypig.dustypig.api.models.BasicFriend
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.RequestStatus
import tv.dustypig.dustypig.api.models.TitleRequestPermissions
import tv.dustypig.dustypig.ui.composables.CreditsData

data class TMDBDetailsUIState (
    val loading: Boolean = true,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val title: String = "",
    val overview: String = "",
    val creditsData: CreditsData = CreditsData(),
    val rated: String = "",
    val year: String = "",
    val isMovie: Boolean = false,
    val available: List<BasicMedia> = listOf(),
    val requestPermissions: TitleRequestPermissions = TitleRequestPermissions.Enabled,
    val requestStatus: RequestStatus = RequestStatus.NotRequested,
    val busy: Boolean = false,
    val showFriendsDialog: Boolean = false,
    val friends: List<BasicFriend> = listOf()
)