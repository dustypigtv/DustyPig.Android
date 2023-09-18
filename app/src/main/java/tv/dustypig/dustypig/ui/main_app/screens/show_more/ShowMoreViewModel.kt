package tv.dustypig.dustypig.ui.main_app.screens.show_more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class ShowMoreViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val mediaRepository: MediaRepository
): ViewModel(), RouteNavigator by routeNavigator {


    private val _uiState = MutableStateFlow(ShowMoreUIState())
    val uiState: StateFlow<ShowMoreUIState> = _uiState.asStateFlow()

    private var _listId: Long = 0

    val itemData: Flow<PagingData<BasicMedia>> = Pager(
        config = PagingConfig(pageSize = 25),
        initialKey = 0,
        pagingSourceFactory = { ShowMorePagingSource(listId = _listId, mediaRepository = mediaRepository) }
    ).flow.cachedIn(viewModelScope)

    init {
        _listId = ShowMorePagingSource.showMoreData.listId
        _uiState.update { it.copy(title = ShowMorePagingSource.showMoreData.title) }
    }


}
