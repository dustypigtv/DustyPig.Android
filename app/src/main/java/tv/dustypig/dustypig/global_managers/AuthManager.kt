package tv.dustypig.dustypig.global_managers

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
class AuthManager @Inject constructor(
    private val settingsManager: SettingsManager
) {

    companion object {
        const val TEST_USER: String = "testuser@dustypig.tv"
        const val TEST_PASSWORD: String = "test password"
    }

    private val _loginState = MutableStateFlow<Boolean?>(null)
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

    fun login(token: String, profileId: Int, isMain: Boolean) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            settingsManager.setToken(token)
            settingsManager.setProfileId(profileId)
            settingsManager.setIsMainProfile(isMain)
            setState(token = token, profileId = profileId, isMain = isMain)
        }
    }

    suspend fun logout() {
        settingsManager.setProfileId(-1)
        settingsManager.setIsMainProfile(false)
        settingsManager.setToken("")
        MediaCacheManager.reset()
        setState(token = "", profileId = 0, isMain = false)
        FCMManager.resetToken()
    }

    fun setTempAuthToken(token:String) {
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

        _loginState.update {
            currentToken != "" && currentProfileId > 0
        }
    }
}