package tv.dustypig.dustypig.ui.main_app.screens.person_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object PersonDetailsNav : NavRoute<PersonDetailsViewModel> {

    const val KEY_TMDB_PERSON_ID = "KEY_MEDIA_ID"

    override val route = "personDetails/{$KEY_TMDB_PERSON_ID}"

    fun getRoute(tmdbId: Int): String = route
        .replace("{$KEY_TMDB_PERSON_ID}", "$tmdbId")


    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_TMDB_PERSON_ID) { type = NavType.IntType }
    )

    @Composable
    override fun viewModel(): PersonDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: PersonDetailsViewModel) = PersonDetailsScreen(vm = viewModel)

}