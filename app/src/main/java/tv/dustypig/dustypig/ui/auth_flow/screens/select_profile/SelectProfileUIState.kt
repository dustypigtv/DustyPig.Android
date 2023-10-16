package tv.dustypig.dustypig.ui.auth_flow.screens.select_profile

import tv.dustypig.dustypig.api.models.BasicProfile


data class SelectProfileUIState(
    val busy: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val profiles: List<BasicProfile> = listOf()
)