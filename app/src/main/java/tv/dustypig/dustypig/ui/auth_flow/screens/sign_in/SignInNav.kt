package tv.dustypig.dustypig.ui.auth_flow.screens.sign_in

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute


object SignInNav : NavRoute<SignInViewModel> {

    override val route = "signIn"

    @Composable
    override fun viewModel(): SignInViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: SignInViewModel) = SignInScreen(viewModel)
}

