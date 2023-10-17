package tv.dustypig.dustypig.ui.main_app.screens.player

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object PlayerNav : NavRoute<PlayerViewModel> {

    override val route= "player"

    @Composable
    override fun viewModel(): PlayerViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: PlayerViewModel) = PlayerScreen(vm = viewModel)

}