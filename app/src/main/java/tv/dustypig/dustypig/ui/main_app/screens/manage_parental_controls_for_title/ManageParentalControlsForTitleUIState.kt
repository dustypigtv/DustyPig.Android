package tv.dustypig.dustypig.ui.main_app.screens.manage_parental_controls_for_title

import tv.dustypig.dustypig.api.models.TitlePermissionInfo

data class ManageParentalControlsForTitleUIState (
    val loading: Boolean = true,
    val showError: Boolean = false,
    val criticalError: Boolean = false,
    val errorMessage: String = "",
    val pendingChanges: Boolean = false,
    val saving: Boolean = false,
    val permissionInfo: TitlePermissionInfo = TitlePermissionInfo(
        titleId = 0,
        profiles = listOf()
    )
)