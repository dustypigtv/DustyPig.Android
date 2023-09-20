package tv.dustypig.dustypig.ui.main_app

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.fcm_manager.FCMManager
import javax.inject.Inject

@HiltViewModel
class AppNavViewModel @Inject constructor(

): ViewModel() {

    val snackbarHostState = SnackbarHostState()

    init {
        viewModelScope.launch {
            FCMManager.inAppAlerts.collectLatest {

                if(it != null) {
                    val result = snackbarHostState.showSnackbar(
                        message = it.message,
                        //actionLabel = "",
                        duration = SnackbarDuration.Long
                    )
                    when(result) {
                        SnackbarResult.ActionPerformed -> {
                        }

                        SnackbarResult.Dismissed -> {
                        }
                    }
                }
            }
        }
    }

}
