package tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object EditProfileNav : NavRoute<EditProfileViewModel> {

    const val KEY_PROFILE_ID = "KEY_PROFILE_ID"

    override val route = "editProfile/{$KEY_PROFILE_ID}"

    fun getRoute(profileId: Int): String = route
        .replace("{$KEY_PROFILE_ID}", "$profileId")

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_PROFILE_ID) { type = NavType.IntType }
    )

    @Composable
    override fun viewModel(): EditProfileViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: EditProfileViewModel) = EditProfileScreen(vm = viewModel)
}