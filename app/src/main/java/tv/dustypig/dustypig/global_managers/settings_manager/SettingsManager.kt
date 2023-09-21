package tv.dustypig.dustypig.global_managers.settings_manager

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SettingsManager @Inject constructor (
    @ApplicationContext context: Context
): SettingsBase(context) {

    companion object {
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val PROFILE_ID_KEY = "profile_id"
        private const val IS_MAIN_PROFILE_KEY = "is_main_profile"
        private const val STORE_DOWNLOADS_EXTERNALLY_KEY = "store_downloads_externally"
        private const val DOWNLOAD_OVER_CELLULAR_KEY = "download_over_cellular"
        private const val SKIP_INTROS_KEY = "skip_intros"
        private const val SKIP_CREDITS_KEY = "skip_credits"
        private const val RECEIVE_NOTIFICATIONS_KEY = "receive_notifications"
    }


    // ***** Global Settings *****

    suspend fun getToken() = getString(AUTH_TOKEN_KEY, "")
    suspend fun setToken(value: String) = setString(AUTH_TOKEN_KEY, value)

    suspend fun getProfileId() = getInt(PROFILE_ID_KEY, 0)
    suspend fun setProfileId(value: Int) = setInt(PROFILE_ID_KEY, value)

    suspend fun getIsMainProfile() = getBoolean(IS_MAIN_PROFILE_KEY, false)
    suspend fun setIsMainProfile(value: Boolean) = setBoolean(IS_MAIN_PROFILE_KEY, value)


    // ***** Profile Settings *****

    private suspend fun profileKey(key: String) = "${key}_${getProfileId()}"

    suspend fun getStoreDownloadsExternally() = getBoolean(profileKey(STORE_DOWNLOADS_EXTERNALLY_KEY), false)
    suspend fun setStoreDownloadsExternally(value: Boolean) = setBoolean(profileKey(STORE_DOWNLOADS_EXTERNALLY_KEY), value)

    suspend fun getDownloadOverCellular() = getBoolean(profileKey(DOWNLOAD_OVER_CELLULAR_KEY), false)
    suspend fun setDownloadOverCellular(value: Boolean) = setBoolean(profileKey(DOWNLOAD_OVER_CELLULAR_KEY), value)

    suspend fun getSkipIntros() = getBoolean(profileKey(SKIP_INTROS_KEY), false)
    suspend fun setSkipIntros(value: Boolean) = setBoolean(profileKey(SKIP_INTROS_KEY), value)

    suspend fun getSkipCredits() = getBoolean(profileKey(SKIP_CREDITS_KEY), false)
    suspend fun setSkipCredits(value: Boolean) = setBoolean(profileKey(SKIP_CREDITS_KEY), value)

    suspend fun getAllowNotifications() = getBoolean(profileKey(RECEIVE_NOTIFICATIONS_KEY), true)
    suspend fun setAllowNotifications(value: Boolean) = setBoolean(profileKey(RECEIVE_NOTIFICATIONS_KEY), value)
}