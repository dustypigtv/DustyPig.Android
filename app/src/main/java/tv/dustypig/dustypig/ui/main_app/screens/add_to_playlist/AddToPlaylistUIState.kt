package tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist

import tv.dustypig.dustypig.api.models.BasicPlaylist

data class AddToPlaylistUIState (
    val busy: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val criticalError: Boolean = false,
    val playlists: List<BasicPlaylist> = listOf()
)