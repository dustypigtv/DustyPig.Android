package tv.dustypig.dustypig

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsManager {

    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    private val PROFILE_ID_KEY = intPreferencesKey("profile_id")
    private val IS_MAIN_PROFILE_KEY = booleanPreferencesKey("is_main_profile")
    private val STORE_DOWNLOADS_EXTERNALLY_KEY = booleanPreferencesKey("store_downloads_externally")
    private val DOWNLOAD_OVER_CELLULAR_KEY = booleanPreferencesKey("download_over_cellular")

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private fun getContext(): Context = DustyPigApplication.Instance.appContext.get()!!



    fun loadToken(): Flow<String> {
        return getContext().dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY] ?: ""
        }
    }

    suspend fun saveToken(token: String) {
        getContext().dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }




    fun loadProfileId() : Flow<Int>{
        return getContext().dataStore.data.map { preferences ->
            preferences[PROFILE_ID_KEY] ?: 0
        }
    }

    suspend fun saveProfileId(profileId: Int){
        getContext().dataStore.edit { preferences ->
            preferences[PROFILE_ID_KEY] = profileId
        }
    }




    fun loadStoreDownloadsExternally(): Flow<Boolean> {

        return getContext().dataStore.data.map { preferences ->
            if(android.os.Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED) {
                preferences[STORE_DOWNLOADS_EXTERNALLY_KEY] ?: true
            }
            else {
                false
            }
        }
    }

    suspend fun saveStoreDownloadsExternally(value: Boolean) {
        getContext().dataStore.edit { preferences ->
            preferences[STORE_DOWNLOADS_EXTERNALLY_KEY] = value
        }
    }




    fun loadIsMainProfile(): Flow<Boolean> {
        return getContext().dataStore.data.map { preferences ->
            preferences[IS_MAIN_PROFILE_KEY] ?: false
        }
    }

    suspend fun saveIsMainProfile(isMainProfile: Boolean) {
        getContext().dataStore.edit { preferences ->
            preferences[IS_MAIN_PROFILE_KEY] = isMainProfile
        }
    }



    fun loadDownloadOverCellular() : Flow<Boolean> {
        return getContext().dataStore.data.map { preferences ->
            preferences[DOWNLOAD_OVER_CELLULAR_KEY] ?: false
        }
    }

    suspend fun saveDownloadOverCellular(value: Boolean) {
        getContext().dataStore.edit { preferences ->
            preferences[DOWNLOAD_OVER_CELLULAR_KEY] = value
        }
    }

}