package tv.dustypig.dustypig.global_managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SettingsManager @Inject constructor (
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val STORE_DOWNLOADS_EXTERNALLY_KEY = "store_downloads_externally"
        private const val DOWNLOAD_OVER_CELLULAR_KEY = "download_over_cellular"
        private const val SKIP_INTROS_KEY = "skip_intros"
        private const val SKIP_CREDITS_KEY = "skip_credits"
        private const val RECEIVE_NOTIFICATIONS_KEY = "receive_notifications"
    }


    private val authTokenPreferencesKey = stringPreferencesKey("auth_token")
    private val profileIdPreferencesKey = intPreferencesKey("profile_id")
    private val isMainProfilePreferenesKey = booleanPreferencesKey("is_main_profile")

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


    // ***** Global Settings *****

    fun loadToken(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[authTokenPreferencesKey] ?: ""
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[authTokenPreferencesKey] = token
        }
    }

    fun loadProfileId() : Flow<Int>{
        return context.dataStore.data.map { preferences ->
            preferences[profileIdPreferencesKey] ?: 0
        }
    }

    suspend fun saveProfileId(profileId: Int){
        context.dataStore.edit { preferences ->
            preferences[profileIdPreferencesKey] = profileId
        }
    }

    fun loadIsMainProfile(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[isMainProfilePreferenesKey] ?: false
        }
    }

    suspend fun saveIsMainProfile(isMainProfile: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isMainProfilePreferenesKey] = isMainProfile
        }
    }





    // ***** Profile Specific Settings Below Here *****


    private suspend fun profileBooleanPreferencesKey(key: String): Preferences.Key<Boolean> {
        val profileId = loadProfileId().first()
        return booleanPreferencesKey("${profileId}_${key}")
    }

    fun loadStoreDownloadsExternally(): Flow<Boolean> {

        return context.dataStore.data.map { preferences ->
            if(android.os.Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED) {
                preferences[profileBooleanPreferencesKey(STORE_DOWNLOADS_EXTERNALLY_KEY)] ?: true
            }
            else {
                false
            }
        }
    }

    suspend fun saveStoreDownloadsExternally(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[profileBooleanPreferencesKey(STORE_DOWNLOADS_EXTERNALLY_KEY)] = value
        }
    }

    fun loadDownloadOverCellular() : Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[profileBooleanPreferencesKey(DOWNLOAD_OVER_CELLULAR_KEY)] ?: false
        }
    }

    suspend fun saveDownloadOverCellular(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[profileBooleanPreferencesKey(DOWNLOAD_OVER_CELLULAR_KEY)] = value
        }
    }

    fun loadSkipIntros(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[profileBooleanPreferencesKey(SKIP_INTROS_KEY)] ?: false
        }
    }

    suspend fun saveSkipIntros(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[profileBooleanPreferencesKey(SKIP_INTROS_KEY)] = value
        }
    }


    fun loadSkipCredits(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[profileBooleanPreferencesKey(SKIP_CREDITS_KEY)] ?: false
        }
    }

    suspend fun saveSkipCredits(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[profileBooleanPreferencesKey(SKIP_CREDITS_KEY)] = value
        }
    }

    fun loadReceiveNotifications(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[profileBooleanPreferencesKey(RECEIVE_NOTIFICATIONS_KEY)] ?: true
        }
    }

    suspend fun saveReceiveNotifications(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[profileBooleanPreferencesKey(RECEIVE_NOTIFICATIONS_KEY)] = value
        }
    }

}