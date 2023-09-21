package tv.dustypig.dustypig.global_managers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tv.dustypig.dustypig.global_managers.settings_manager.SettingsManager
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthManager @Inject constructor(private val settingsManager: SettingsManager) {

    companion object {
        const val LOGIN_STATE_UNKNOWN: Int = 0
        const val LOGIN_STATE_LOGGED_IN: Int = 1
        const val LOGIN_STATE_LOGGED_OUT: Int = 2

        const val TEST_USER: String = "testuser@dustypig.tv"
        const val TEST_PASSWORD: String = "test password"
    }

    var loginState by mutableIntStateOf(LOGIN_STATE_UNKNOWN)
        private set

    var currentToken: String = ""
        private set

    var currentProfileId: Int = 0
        private set

    var currentProfileIsMain: Boolean = false
        private set

    @OptIn(DelicateCoroutinesApi::class)
    fun init() {
        GlobalScope.launch {
            setState(
                token = settingsManager.getToken(),
                profileId = settingsManager.getProfileId(),
                isMain = settingsManager.getIsMainProfile()
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun setAuthState(token: String, profileId: Int, isMain: Boolean) {
        GlobalScope.launch {
            settingsManager.setToken(token)
            settingsManager.setProfileId(profileId)
            settingsManager.setIsMainProfile(isMain)
            setState(token = token, profileId = profileId, isMain = isMain)
        }
    }

    fun logout() {
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

        loginState =
            if(currentToken != "" && currentProfileId > 0) LOGIN_STATE_LOGGED_IN else LOGIN_STATE_LOGGED_OUT
    }
}