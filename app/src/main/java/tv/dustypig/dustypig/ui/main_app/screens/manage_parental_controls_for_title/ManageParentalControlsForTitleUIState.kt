package tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title

import tv.dustypig.dustypig.api.models.ProfileTitleOverrideInfo

data class ManageParentalControlsForTitleUIState (

    //Data
    val busy: Boolean = true,
    val showErrorDialog: Boolean = false,
    val criticalError: Boolean = false,
    val errorMessage: String? = null,
    val pendingChanges: Boolean = false,
    val subProfiles: List<ProfileTitleOverrideInfo> = listOf(),
    val friendProfiles: List<ProfileTitleOverrideInfo> = listOf(),

    //Events
    val onPopBackStack: () -> Unit = { },
    val onTogglePermission: (profileId: Int) -> Unit = { },
    val onHideError: () -> Unit = { }
)