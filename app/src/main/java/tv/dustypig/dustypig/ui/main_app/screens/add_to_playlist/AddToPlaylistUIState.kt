package tv.dustypig.dustypig.ui.main_app.screens.add_to_playlist

import tv.dustypig.dustypig.api.models.BasicPlaylist

data class AddToPlaylistUIState (

    //Data
    val busy: Boolean = true,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val criticalError: Boolean = false,
    val playlists: List<BasicPlaylist> = listOf(),
    val addingSeries: Boolean = false,

    //Events
    val onPopBackStack: () -> Unit = { },
    val onHideError: () -> Unit = { },
    val onNewPlaylist: (name: String, autoAddEpisodes: Boolean) -> Unit = { _, _ -> },
    val onSelectPlaylist: (id: Int, autoAddEpisodes: Boolean) -> Unit = { _, _ -> },

)