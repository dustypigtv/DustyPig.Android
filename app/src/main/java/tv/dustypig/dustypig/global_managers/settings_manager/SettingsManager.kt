package tv.dustypig.dustypig.global_managers.settings_manager

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {


    companion object {

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val PROFILE_ID_KEY = "profile_id"
        private const val IS_MAIN_PROFILE_KEY = "is_main_profile"
        private const val DOWNLOAD_OVER_CELLULAR_KEY = "download_over_cellular"
        private const val SKIP_INTROS_KEY = "skip_intros"
        private const val SKIP_CREDITS_KEY = "skip_credits"
        private const val THEME_KEY = "theme"
        private const val ALLOW_NOTIFICATIONS_KEY = "allow_notifications"
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val DEVICE_ID_KEY = "device_id"
        private const val LAST_LOGIN_EMAIL_KEY = "last_login_email"


        // For global settings
        private val authTokenPreferencesKey = stringPreferencesKey(AUTH_TOKEN_KEY)
        private val profileIdPreferencesKey = intPreferencesKey(PROFILE_ID_KEY)
        private val isMainProfilePreferencesKey = booleanPreferencesKey(IS_MAIN_PROFILE_KEY)
        private val deviceIdKey = stringPreferencesKey(DEVICE_ID_KEY)
        private val lastLoginEmailKey = stringPreferencesKey(LAST_LOGIN_EMAIL_KEY)
    }


    // ***** Global Settings *****

    fun getSystemLevelAllowNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }


    //suspend fun getLastLoginEmail() = context.dataStore.data.map { it[lastLoginEmailKey] ?: "" }.first()
    suspend fun setLastLoginEmail(value: String) =
        context.dataStore.edit { it[lastLoginEmailKey] = value }

    val lastLoginEmailFlow = context.dataStore.data.map { it[lastLoginEmailKey] ?: "" }

    suspend fun getToken() =
        context.dataStore.data.map { it[authTokenPreferencesKey] ?: "" }.first()

    suspend fun setToken(value: String) =
        context.dataStore.edit { it[authTokenPreferencesKey] = value }

    suspend fun getProfileId() =
        context.dataStore.data.map { it[profileIdPreferencesKey] ?: 0 }.first()

    suspend fun setProfileId(value: Int) =
        context.dataStore.edit { it[profileIdPreferencesKey] = value }

    val profileIdFlow = context.dataStore.data.map { it[profileIdPreferencesKey] ?: 0 }

    suspend fun getIsMainProfile() =
        context.dataStore.data.map { it[isMainProfilePreferencesKey] ?: false }.first()

    suspend fun setIsMainProfile(value: Boolean) =
        context.dataStore.edit { it[isMainProfilePreferencesKey] = value }

    val profileIsMainFlow = context.dataStore.data.map { it[isMainProfilePreferencesKey] ?: false }

    suspend fun getDeviceId(): String {
        var ret = context.dataStore.data.map { it[deviceIdKey] ?: "" }.first()
        if (ret.isBlank()) {
            ret = UUID.randomUUID().toString().replace("-", "")
            context.dataStore.edit { it[deviceIdKey] = ret }
        }
        return ret
    }


    // ***** Profile Settings *****

    private fun profileKey(key: String, profileId: Int) = "${key}_${profileId}"
    private suspend fun profileKey(key: String) = profileKey(key, getProfileId())

    private suspend fun downloadOverMobilePreferencesKey() =
        booleanPreferencesKey(profileKey(DOWNLOAD_OVER_CELLULAR_KEY))

    suspend fun getDownloadOverMobile() =
        context.dataStore.data.map { it[downloadOverMobilePreferencesKey()] ?: false }.first()

    suspend fun setDownloadOverMobile(value: Boolean) =
        context.dataStore.edit { it[downloadOverMobilePreferencesKey()] = value }

    val downloadOverMobileFlow =
        context.dataStore.data.map { it[downloadOverMobilePreferencesKey()] ?: false }


    private suspend fun skipIntrosPreferencesKey() =
        booleanPreferencesKey(profileKey(SKIP_INTROS_KEY))

    suspend fun getSkipIntros() =
        context.dataStore.data.map { it[skipIntrosPreferencesKey()] ?: false }.first()

    suspend fun setSkipIntros(value: Boolean) =
        context.dataStore.edit { it[skipIntrosPreferencesKey()] = value }


    private suspend fun skipCreditsPreferencesKey() =
        booleanPreferencesKey(profileKey(SKIP_CREDITS_KEY))

    suspend fun getSkipCredits() =
        context.dataStore.data.map { it[skipCreditsPreferencesKey()] ?: false }.first()

    suspend fun setSkipCredits(value: Boolean) =
        context.dataStore.edit { it[skipCreditsPreferencesKey()] = value }


    private suspend fun themePreferencesKey() = intPreferencesKey(profileKey(THEME_KEY))
    suspend fun setTheme(theme: Themes) =
        context.dataStore.edit { it[themePreferencesKey()] = theme.ordinal }

    val themeFlow =
        context.dataStore.data.map { Themes.fromOrdinal(it[themePreferencesKey()] ?: 0) }


    private suspend fun searchHistoryPreferencesKey() =
        stringPreferencesKey(profileKey(SEARCH_HISTORY_KEY))

    suspend fun setSearchHistory(value: List<String>) =
        context.dataStore.edit { it[searchHistoryPreferencesKey()] = JSONArray(value).toString() }

    val searchHistoryFlow = context.dataStore.data.map {
        val lst = arrayListOf<String>()
        try {
            val value = it[searchHistoryPreferencesKey()] ?: "[]"
            val json = JSONArray(value)
            for (i in 0 until json.length()) {
                lst.add(json[i].toString())
            }
        } catch (_: Exception) {
        }

        lst
    }


    private suspend fun allowNotificationsKey() =
        booleanPreferencesKey(profileKey(ALLOW_NOTIFICATIONS_KEY))

    private fun allowNotificationsKey(id: Int) =
        booleanPreferencesKey(profileKey(ALLOW_NOTIFICATIONS_KEY, id))

    suspend fun setAllowNotifications(value: Boolean) =
        context.dataStore.edit { it[allowNotificationsKey()] = value }

    suspend fun getAllowNotifications(): Boolean {
        return try {
            getSystemLevelAllowNotifications() &&
                    context.dataStore.data.map { it[allowNotificationsKey()] ?: true }.first()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getAllowNotifications(id: Int): Boolean {
        return try {
            getSystemLevelAllowNotifications() &&
                    context.dataStore.data.map { it[allowNotificationsKey(id)] ?: true }.first()
        } catch (_: Exception) {
            false
        }
    }

}