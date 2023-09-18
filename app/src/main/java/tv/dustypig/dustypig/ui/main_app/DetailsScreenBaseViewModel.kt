package tv.dustypig.dustypig.ui.main_app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.composables.TitleInfoData

abstract class DetailsScreenBaseViewModel constructor(
    private val routeNavigator: RouteNavigator,
    internal val downloadManager: DownloadManager
): ViewModel(), RouteNavigator by routeNavigator {

    private val _titleInfoUIState = MutableStateFlow(TitleInfoData(downloadManager = downloadManager))

    fun getTitleInfoUIStateForUpdate() = _titleInfoUIState

    val titleInfoUIState: StateFlow<TitleInfoData> = _titleInfoUIState.asStateFlow()

    abstract val mediaId: Int
}