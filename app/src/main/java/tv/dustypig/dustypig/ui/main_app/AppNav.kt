package tv.dustypig.dustypig.ui.main_app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tv.dustypig.dustypig.ui.main_app.screens.HomeScreen


object AppScreenDestinations {
    const val Home = "home"
}

@Composable
fun AppNav(){

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppScreenDestinations.Home){

        composable(AppScreenDestinations.Home){
            HomeScreen(navController)
        }
    }
}