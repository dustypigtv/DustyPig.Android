package tv.dustypig.dustypig.global_managers

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.media_cache_manager.MediaCacheManager
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthManager @Inject constructor(private val settingsManager: SettingsManager) {

    companion object {
        private const val TAG = "AuthManager"

        const val LOGIN_STATE_UNKNOWN: Int = 0
        const val LOGIN_STATE_LOGGED_IN: Int = 1
        const val LOGIN_STATE_LOGGED_OUT: Int = 2
        const val LOGIN_STATE_SWITCHING_PROFILES: Int = 3

        const val TEST_USER: String = "testuser@dustypig.tv"
        const val TEST_PASSWORD: String = "test password"
    }

    private val _loginState = MutableStateFlow(LOGIN_STATE_UNKNOWN)
    val loginState = _loginState.asStateFlow()

    var currentToken: String = ""
        private set

    var currentProfileId: Int = 0
        private set

    var currentProfileIsMain: Boolean = false
        private set

    fun init() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            setState(
                token = settingsManager.getToken(),
                profileId = settingsManager.getProfileId(),
                isMain = settingsManager.getIsMainProfile()
            )
        }
    }

    fun setAuthState(token: String, profileId: Int, isMain: Boolean) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            settingsManager.setToken(token)
            settingsManager.setProfileId(profileId)
            settingsManager.setIsMainProfile(isMain)
            setState(token = token, profileId = profileId, isMain = isMain)
        }
    }

    fun switchProfileBegin(token: String, profileId: Int, isMain: Boolean) {
        MediaCacheManager.reset()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            settingsManager.setToken(token)
            settingsManager.setProfileId(profileId)
            settingsManager.setIsMainProfile(isMain)
            _loginState.update {
                LOGIN_STATE_SWITCHING_PROFILES
            }
        }
    }

    fun switchProfileEnd() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            setState(
                token = settingsManager.getToken(),
                profileId = settingsManager.getProfileId(),
                isMain = settingsManager.getIsMainProfile()
            )
        }
    }


    fun logout() {
        MediaCacheManager.reset()
        setAuthState(token = "", profileId = 0, isMain = false)
    }

    //Use this to set a temp auth token between logging int and selecting the profile
    fun setTempAuthToken(token:String) {
        currentToken = token
        currentProfileId = 0
        currentProfileIsMain = false
    }


    private fun setState(token: String, profileId: Int, isMain: Boolean) {
        currentToken = token
        currentProfileId = profileId
        currentProfileIsMain = isMain

        _loginState.update {
            if(currentToken != "" && currentProfileId > 0)
                LOGIN_STATE_LOGGED_IN
            else
                LOGIN_STATE_LOGGED_OUT
        }

        Log.d(TAG, token)
    }
}