package tv.dustypig.dustypig.ui.main_app.screens.settings.friends_settings

import tv.dustypig.dustypig.api.models.BasicFriend

data class FriendsSettingsUIState(

    //Data
    val busy: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val friends: List<BasicFriend> = listOf(),
    val inviteBusy: Boolean = false,
    val showInviteSuccessDialog: Boolean = false,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onNavToFriendDetails: (id: Int) -> Unit = { },
    val onHideDialogs: () -> Unit = { },
    val onAddFriend: (email: String) -> Unit = { }
)
