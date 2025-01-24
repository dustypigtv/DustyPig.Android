package tv.dustypig.dustypig.ui.main_app.screens.person_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import tv.dustypig.dustypig.nav.NavRoute

object PersonDetailsNav : NavRoute<PersonDetailsViewModel> {

    const val KEY_TMDB_PERSON_ID = "KEY_MEDIA_ID"
    const val KEY_BASIC_CACHE_ID = "KEY_BASIC_CACHE_ID"

    override val route = "personDetails/{$KEY_TMDB_PERSON_ID}/{$KEY_BASIC_CACHE_ID}"

    fun getRoute(tmdbId: Int, basicCacheId: String): String = route
        .replace("{$KEY_TMDB_PERSON_ID}", "$tmdbId")
        .replace("{$KEY_BASIC_CACHE_ID}", basicCacheId)


    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_TMDB_PERSON_ID) { type = NavType.IntType },
        navArgument(KEY_BASIC_CACHE_ID) { type = NavType.StringType }
    )

    @Composable
    override fun viewModel(): PersonDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: PersonDetailsViewModel) = PersonDetailsScreen(vm = viewModel)

}