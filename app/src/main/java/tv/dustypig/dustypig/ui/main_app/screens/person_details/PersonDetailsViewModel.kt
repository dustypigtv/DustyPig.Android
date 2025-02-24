package tv.dustypig.dustypig.ui.main_app.screens.person_details

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.api.repositories.TMDBRepository
import tv.dustypig.dustypig.global_managers.cast_manager.CastManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.nav.getOrThrow
import java.text.DateFormat
import java.text.SimpleDateFormat
import javax.inject.Inject

@SuppressLint("SimpleDateFormat")
@HiltViewModel
@OptIn(UnstableApi::class)
class PersonDetailsViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle,
    castManager: CastManager,
    tmdbRepository: TMDBRepository
) : ViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(
        PersonDetailsUIState(
            castManager = castManager,
            onPopBackStack = ::popBackStack,
            onHideError = ::hideError,
        )
    )

    val uiState: StateFlow<PersonDetailsUIState> = _uiState.asStateFlow()

    private val _tmdbPersonId: Int = savedStateHandle.getOrThrow(PersonDetailsNav.KEY_TMDB_PERSON_ID)

    init {

        viewModelScope.launch {
            try {
                val data = tmdbRepository.getPerson(_tmdbPersonId)
                val df: DateFormat = SimpleDateFormat("EEE, MMM d, yyyy")
                _uiState.update {
                    it.copy(
                        loading = false,
                        placeOfBirth = data.placeOfBirth,
                        biography = data.biography,
                        knownFor = data.knownFor,
                        birthday = if (data.birthday == null) null else df.format(data.birthday),
                        deathday = if (data.deathday == null) null else df.format(data.deathday),
                        available = data.available,
                        otherTitles = data.otherTitles
                    )
                }
            } catch (ex: Exception) {
                ex.logToCrashlytics()
                _uiState.update {
                    it.copy(
                        loading = false,
                        showErrorDialog = true,
                        errorMessage = ex.localizedMessage,
                        criticalError = true
                    )
                }
            }
        }
    }


    private fun hideError() {
        if (_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }
}