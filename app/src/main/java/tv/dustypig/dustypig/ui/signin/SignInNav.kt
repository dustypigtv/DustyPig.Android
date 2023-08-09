package tv.dustypig.dustypig.ui.signin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tv.dustypig.dustypig.ui.signin.screens.SelectProfileScreen
import tv.dustypig.dustypig.ui.signin.screens.SignInScreen
import tv.dustypig.dustypig.ui.signin.screens.SignUpScreen

object SignInDestinations {
    const val SignIn = "signIn"
    const val SignUp = "signUp"
    const val SelectProfile = "selectProfile"
}

@Composable
fun SignInNav(){

    val navController = rememberNavController()

    val email = remember {
        mutableStateOf("")
    }

    val password = remember {
        mutableStateOf("")
    }

    NavHost(navController = navController, startDestination = SignInDestinations.SignIn) {

        composable(SignInDestinations.SignIn) {
            SignInScreen(navController, email, password)
        }

        composable(SignInDestinations.SignUp) {
            SignUpScreen(navController, email)
        }

        composable(SignInDestinations.SelectProfile) {
            SelectProfileScreen(navController)
        }
    }
}