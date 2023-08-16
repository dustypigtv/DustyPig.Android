package tv.dustypig.dustypig.ui.main_app.screens.search

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute


object SearchNav : NavRoute<SearchViewModel> {

    override val route = "search"

    @Composable
    override fun viewModel(): SearchViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: SearchViewModel) = SearchScreen(viewModel)
}
