package tv.dustypig.dustypig.ui.main_app.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.SearchRequest
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val mediaRepository: MediaRepository
): ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(SearchUIState())
    val uiState: StateFlow<SearchUIState> = _uiState.asStateFlow()
    private var _lastQuery: String = ""

    fun search(query: String) {

        val ltquery = query.trim().lowercase()

        _uiState.update {
            it.copy(
                emptyQuery = ltquery.isBlank(),
                loading = !(ltquery.isBlank() || ltquery == _lastQuery) ,
                progressOnly = _lastQuery.isBlank() && ltquery.isNotBlank() && !it.hasResults,
                hasResults = if(ltquery.isNotBlank()) it.hasResults else false,
                availableItems = if(ltquery.isBlank()) listOf() else it.availableItems,
                tmdbItems = if(ltquery.isBlank()) listOf() else it.tmdbItems
            )
        }

        if(ltquery.isBlank() || ltquery == _lastQuery)
            return

        _lastQuery = ltquery

        viewModelScope.launch {
            try{
                val response = mediaRepository.search(SearchRequest(query = ltquery, searchTMDB = true))

                _uiState.update {
                    it.copy(
                        loading = false,
                        allowTMDB = response.otherTitlesAllowed,
                        hasResults = !(response.available.isNullOrEmpty() || response.otherTitles.isNullOrEmpty()),
                        progressOnly = false,
                        availableItems = response.available ?: listOf(),
                        tmdbItems = response.otherTitles ?: listOf()
                    )
                }

            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        showErrorDialog = true,
                        errorMessage = ex.localizedMessage
                    )
                }
            }
        }
    }

    fun hideError() {
        _uiState.update {
            it.copy(showErrorDialog = false)
        }
    }

    /**
     * Put this here to survive recomp
     */
    fun updateTabIndex(idx: Int) {
        _uiState.update {
            it.copy(tabIndex = idx)
        }
    }
}