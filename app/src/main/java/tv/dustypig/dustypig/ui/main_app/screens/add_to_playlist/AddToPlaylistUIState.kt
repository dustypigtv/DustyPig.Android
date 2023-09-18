package tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist

import tv.dustypig.dustypig.api.models.BasicPlaylist

data class AddToPlaylistUIState (
    val loading: Boolean = true,
    val busy: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val playlists: List<BasicPlaylist> = listOf()
)