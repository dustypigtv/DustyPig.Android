package tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings.friend_details_settings

import tv.dustypig.dustypig.api.models.BasicLibrary

data class ShareableLibrary(
    val id: Int,
    val name: String,
    val isTV: Boolean,
    val shared: Boolean
)

data class FriendDetailsSettingsUIState(

    //Data
    val busy: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val displayName: String = "",
    val avatarUrl: String = "",
    val libsSharedWithMe: List<BasicLibrary> = listOf(),
    val myLibs: List<ShareableLibrary> = listOf(),

    //Events
    val onPopBackStack: () -> Unit = { },
    val onHideError: () -> Unit = { },
    val onChangeDisplayName: (newName: String) -> Unit = { },
    val onToggleLibraryShare: (libraryId: Int) -> Unit = { },
    val onUnfriend: () -> Unit = { }
)
