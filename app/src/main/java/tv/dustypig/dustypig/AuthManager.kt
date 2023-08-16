package tv.dustypig.dustypig

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object AuthManager {

    private const val LOGIN_STATE_UNKNOWN: Int = 0
    const val LOGIN_STATE_LOGGED_IN: Int = 1
    const val LOGIN_STATE_LOGGED_OUT: Int = 2

    const val TEST_USER: String = "testuser@dustypig.tv"
    const val TEST_PASSWORD: String = "test password"

    var loginState by mutableStateOf(LOGIN_STATE_UNKNOWN)
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
                SettingsManager.loadToken().first(),
                SettingsManager.loadProfileId().first(),
                SettingsManager.loadIsMainProfile().first()
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun setAuthState(token: String, profileId: Int, isMain: Boolean) {
        GlobalScope.launch {
            SettingsManager.saveToken(token)
            SettingsManager.saveProfileId(profileId)
            SettingsManager.saveIsMainProfile(isMain)
            setState(token, profileId, isMain)
        }
    }

    fun logout() = setAuthState("", 0, false)

    //Use this to set a temp auth token between logging int and selecting the profile
    fun setTempAuthToken(token:String){
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