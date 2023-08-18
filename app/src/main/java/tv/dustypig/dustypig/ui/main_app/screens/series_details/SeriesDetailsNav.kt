package tv.dustypig.dustypig.ui.main_app.screens.series_details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import tv.dustypig.dustypig.nav.NavRoute

object SeriesDetailsNav : NavRoute<SeriesDetailsViewModel> {

    override val route = "seriesDetails"

    @Composable
    override fun viewModel(): SeriesDetailsViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: SeriesDetailsViewModel) = SeriesDetailsScreen(vm = viewModel)

}