package tv.dustypig.dustypig.global_managers.settings_manager

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.io.File


abstract class SettingsBase constructor (
    private val context: Context,
    protected val fileName: String = "settings.json"
) {
    private class SettingsMaps {
        val intMap = mutableMapOf<String, Int>()
        val boolMap = mutableMapOf<String, Boolean>()
        val stringMap = mutableMapOf<String, String>()

        fun getValue(key: String, default: Int) = intMap[key] ?: default

        fun getValue(key: String, default: Boolean) = boolMap[key] ?: default

        fun getValue(key: String, default: String) = stringMap[key] ?: default
    }

    private fun getFile(): File {
        if(fileName.isBlank())
            throw Exception("Invalid filename: <Blank filename>")
        return File(context.filesDir, fileName)
    }

    private suspend fun loadSettings(): SettingsMaps {
        return withContext(Dispatchers.IO + SupervisorJob()) {
            try{
                Gson().fromJson(getFile().readText(), SettingsMaps::class.java)
            } catch (_: Exception) {
                SettingsMaps()
            }
        }
    }

    private suspend fun saveSettings(settingsMaps: SettingsMaps) {
        withContext(Dispatchers.IO + SupervisorJob()) {
            try{
                getFile().writeText(Gson().toJson(settingsMaps))
            } catch (_: Exception) {
            }
        }
    }

    protected suspend fun getBoolean(key: String, default: Boolean) = loadSettings().getValue(key, default)

    protected suspend fun setBoolean(key: String, value: Boolean) {
        val settingsMaps = loadSettings()
        settingsMaps.boolMap[key] = value
        saveSettings(settingsMaps)
    }

    protected suspend fun getInt(key: String, default: Int) = loadSettings().getValue(key, default)

    protected suspend fun setInt(key: String, value: Int) {
        val settingsMaps = loadSettings()
        settingsMaps.intMap[key] = value
        saveSettings(settingsMaps)
    }

    protected suspend fun getString(key: String, default: String) = loadSettings().getValue(key, default)

    protected suspend fun setString(key: String, value: String) {
        val settingsMaps = loadSettings()
        settingsMaps.stringMap[key] = value
        saveSettings(settingsMaps)
    }
}
