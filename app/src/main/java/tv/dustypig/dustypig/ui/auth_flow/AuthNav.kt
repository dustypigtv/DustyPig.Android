package tv.dustypig.dustypig.ui.auth_flow

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import tv.dustypig.dustypig.ui.auth_flow.screens.select_profile.SelectProfileNav
import tv.dustypig.dustypig.ui.auth_flow.screens.sign_in.SignInNav
import tv.dustypig.dustypig.ui.auth_flow.screens.sign_up.SignUpNav


@Composable
fun AuthNav() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SignInNav.route) {
        SignInNav.composable(this, navController)
        SignUpNav.composable(this, navController)
        SelectProfileNav.composable(this, navController)
    }
}