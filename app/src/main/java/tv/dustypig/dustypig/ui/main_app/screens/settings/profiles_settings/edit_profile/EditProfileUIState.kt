package tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile

import tv.dustypig.dustypig.api.models.BasicLibrary
import tv.dustypig.dustypig.api.models.MovieRatings
import tv.dustypig.dustypig.api.models.TVRatings
import tv.dustypig.dustypig.api.models.TitleRequestPermissions

data class EditProfileUIState(

    //Data
    val busy: Boolean = true,
    val loadingComplete: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val addMode: Boolean = false, //True for adding a new profile, false for editing an existing one
    val selfMode: Boolean = false, //True when editing own profile
    val name: String = "",
    val hasPin: Boolean = false,
    val lockedState: LockedState = LockedState.Unlocked,
    val avatarUrl: String = "",
    val libraries: List<BasicLibrary> = listOf(),
    val selectedLibraryIds: List<Int> = listOf(),
    val maxMovieRating: MovieRatings = MovieRatings.G,
    val maxTVRating: TVRatings = TVRatings.Y,
    val titleRequestPermissions: TitleRequestPermissions = TitleRequestPermissions.RequiresAuthorization,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onSetError: (ex: Exception, criticalError: Boolean) -> Unit = { _, _ -> },
    val onHideError: () -> Unit = { },
    val onInfoLoaded: () -> Unit = { },
    val onDeleteProfile: () -> Unit = { },

    val onSaveProfile: (
        name: String,
        pin: String,
        deletePin: Boolean,
        maxMovieRating: MovieRatings,
        maxTVRating: TVRatings,
        titleRequestPermissions: TitleRequestPermissions,
        lockedState: LockedState,
        selectedLibs: List<Int>,
        avatarFile: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _ -> },

    )