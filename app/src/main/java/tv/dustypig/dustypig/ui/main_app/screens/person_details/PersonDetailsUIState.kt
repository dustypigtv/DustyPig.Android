package tv.dustypig.dustypig.ui.main_app.screens.person_details

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicTMDB
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager

@OptIn(UnstableApi::class)
data class PersonDetailsUIState(
    val loading: Boolean = true,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val castManager: CastManager? = null,
    val avatarUrl: String? = null,
    val name: String? = null,
    val birthday: String? = null,
    val deathday: String? = null,
    val placeOfBirth: String? = null,
    val biography: String? = null,
    val knownFor: String? = null,
    val available: List<BasicMedia> = listOf(),
    val otherTitles: List<BasicTMDB> = listOf(),

    //Events
    val onPopBackStack: () -> Unit = { },
    val onHideError: () -> Unit = { },
)
