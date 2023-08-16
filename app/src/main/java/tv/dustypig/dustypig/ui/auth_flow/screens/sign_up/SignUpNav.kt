package tv.dustypig.dustypig.ui.auth_flow.screens.sign_up

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute


object SignUpNav : NavRoute<SignUpViewModel> {

    override val route = "signUp"

    @Composable
    override fun viewModel(): SignUpViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: SignUpViewModel) = SignUpScreen(viewModel)
}