package tv.dustypig.dustypig.ui.auth_flow

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import tv.dustypig.dustypig.ui.auth_flow.screens.select_profile.SelectProfileScreenRoute
import tv.dustypig.dustypig.ui.auth_flow.screens.sign_in.SignInScreenRoute
import tv.dustypig.dustypig.ui.auth_flow.screens.sign_up.SignUpScreenRoute


@Composable
fun AuthNav(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SignInScreenRoute.route) {
        SignInScreenRoute.composable(this, navController)
        SignUpScreenRoute.composable(this, navController)
        SelectProfileScreenRoute.composable(this, navController)
    }
}