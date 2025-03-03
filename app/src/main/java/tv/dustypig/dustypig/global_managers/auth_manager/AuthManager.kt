package tv.dustypig.dustypig.global_managers.auth_manager

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.FCMManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthManager @Inject constructor(
    private val settingsManager: SettingsManager
) {

    companion object {
        const val TAG = "AuthManager"

        const val TEST_USER: String = "testuser@dustypig.tv"
        const val TEST_PASSWORD: String = "test password"
    }

    private val _loginState = MutableStateFlow(AuthStates.Nada)
    val loginState = _loginState.asStateFlow()

    var currentToken: String = ""
        private set

    var currentProfileId: Int = 0
        private set

    var currentProfileIsMain: Boolean = false
        private set

    var changeProfilesPhase2Enabled: Boolean = false

    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init() {

        _scope.launch {
            loginState.collect {
                Log.d(TAG, "LoginState: $it")

                if(it == AuthStates.ChangingProfiles) {
                    while(!changeProfilesPhase2Enabled) {
                        delay(100)
                    }
                    setState(
                        token = settingsManager.getToken(),
                        profileId = settingsManager.getProfileId(),
                        isMain = settingsManager.getIsMainProfile()
                    )
                }
            }
        }

        _scope.launch {
            setState(
                token = settingsManager.getToken(),
                profileId = settingsManager.getProfileId(),
                isMain = settingsManager.getIsMainProfile()
            )
        }

    }

    fun login(token: String, profileId: Int, isMain: Boolean) {
        _scope.launch {
            settingsManager.setToken(token)
            settingsManager.setProfileId(profileId)
            settingsManager.setIsMainProfile(isMain)
            setState(token = token, profileId = profileId, isMain = isMain)
        }
    }


    fun switchProfile (token: String, profileId: Int, isMain: Boolean) {
        changeProfilesPhase2Enabled = false
        _scope.launch {
            settingsManager.setToken(token)
            settingsManager.setProfileId(profileId)
            settingsManager.setIsMainProfile(isMain)
            _loginState.tryEmit(AuthStates.ChangingProfiles)
        }
    }

    suspend fun logout() {
        settingsManager.setProfileId(-1)
        settingsManager.setIsMainProfile(false)
        settingsManager.setToken("")
//        setState(token = "", profileId = 0, isMain = false)
        FCMManager.resetToken()
    }

    fun setTempAuthToken(token: String) {
        currentToken = token
    }

    suspend fun setAuthToken(token: String) {
        currentToken = token
        settingsManager.setToken(token)
    }

    private fun setState(token: String, profileId: Int, isMain: Boolean) {
        currentToken = token
        currentProfileId = profileId
        currentProfileIsMain = isMain

        _loginState.tryEmit(
            if(currentToken != "" && currentProfileId > 0) {
                AuthStates.LoggedIn
            } else {
                AuthStates.LoggedOut
            }
        )
    }
}