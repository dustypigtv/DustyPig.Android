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

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    fun loadToken(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY] ?: ""
        }
    }

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }

    fun loadProfileId(context: Context) : Flow<Int>{
        return context.dataStore.data.map { preferences ->
            preferences[PROFILE_ID_KEY] ?: 0
        }
    }

    suspend fun saveProfileId(context: Context, profileId: Int){
        context.dataStore.edit { preferences ->
            preferences[PROFILE_ID_KEY] = profileId
        }
    }

    fun loadIsMainProfile(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[IS_MAIN_PROFILE_KEY] ?: false
        }
    }

    suspend fun saveIsMainProfile(context: Context, isMainProfile: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_MAIN_PROFILE_KEY] = isMainProfile
        }
    }

}