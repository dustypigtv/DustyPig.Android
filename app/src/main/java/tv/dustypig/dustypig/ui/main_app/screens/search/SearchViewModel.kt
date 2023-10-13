package tv.dustypig.dustypig.ui.main_app.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.models.SearchRequest
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val mediaRepository: MediaRepository,
    private val settingsManager: SettingsManager
): ViewModel(), RouteNavigator by routeNavigator {

    companion object {
        private const val TAG = "SearchViewModel"
    }

    private val _uiState = MutableStateFlow(SearchUIState())
    val uiState: StateFlow<SearchUIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.searchHistoryFlow.collectLatest { history ->
                _uiState.update {
                    it.copy(history = history)
                }
            }
        }
    }

    fun search() {

        val query = _uiState.value.query.trim().lowercase()

        _uiState.update {
            it.copy(
                emptyQuery = query.isBlank(),
                busy = query.isNotBlank(),
                progressOnly = query.isNotBlank() && !it.hasResults,
                hasResults = if(query.isNotBlank()) it.hasResults else false,
                availableItems = if(query.isBlank()) listOf() else it.availableItems,
                tmdbItems = if(query.isBlank()) listOf() else it.tmdbItems
            )
        }

        if(query.isBlank())
            return

        viewModelScope.launch {
            try {
                val hist = ArrayList<String>()
                hist.add(query)
                for(q in _uiState.value.history) {
                    if(q != query)
                        if(hist.count() < 25)
                            hist.add(q)
                }
                settingsManager.setSearchHistory(hist)
            } catch (ex: Exception) {
                Log.d(TAG, ex.message ?: "Unknown Error")
            }

            try {
                val response = mediaRepository.search(SearchRequest(query = query, searchTMDB = true))

                _uiState.update {
                    it.copy(
                        busy = false,
                        allowTMDB = response.otherTitlesAllowed,
                        hasResults = !(response.available.isNullOrEmpty() || response.otherTitles.isNullOrEmpty()),
                        progressOnly = false,
                        availableItems = response.available ?: listOf(),
                        tmdbItems = response.otherTitles ?: listOf()
                    )
                }

            } catch (ex: Exception) {
                ex.logToCrashlytics()
                _uiState.update {
                    it.copy(
                        busy = false,
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

    fun updateQuery(query: String) {
        _uiState.update {
            it.copy(query = query)
        }
    }

    /**
     * Set here to survive recomposition
     */
    fun updateTabIndex(index: Int) {
        _uiState.update {
            it.copy(tabIndex = index)
        }
    }
}