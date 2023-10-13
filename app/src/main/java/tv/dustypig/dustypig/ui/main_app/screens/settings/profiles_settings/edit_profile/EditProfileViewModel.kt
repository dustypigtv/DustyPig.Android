package tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import tv.dustypig.dustypig.api.models.BasicLibrary
import tv.dustypig.dustypig.api.models.CreateProfile
import tv.dustypig.dustypig.api.models.DetailedProfile
import tv.dustypig.dustypig.api.models.MovieRatings
import tv.dustypig.dustypig.api.models.ProfileLibraryLink
import tv.dustypig.dustypig.api.models.TVRatings
import tv.dustypig.dustypig.api.models.TitleRequestPermissions
import tv.dustypig.dustypig.api.models.UpdateProfile
import tv.dustypig.dustypig.api.repositories.LibrariesRepository
import tv.dustypig.dustypig.api.repositories.ProfilesRepository
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.logToCrashlytics
import tv.dustypig.dustypig.nav.RouteNavigator
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.ProfilesSettingsViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profilesRepository: ProfilesRepository,
    private val librariesRepository: LibrariesRepository,
    private val authManager: AuthManager,
    routeNavigator: RouteNavigator
): ViewModel(), RouteNavigator by routeNavigator {

    companion object {
        var preloadAvatar = ""
        var selectedProfileId = 0
    }

    private val _uiState = MutableStateFlow(EditProfileUIState())
    val uiState = _uiState.asStateFlow()

    private var allLibIds = arrayListOf<Int>()

    init {
        _uiState.update {
            it.copy(
                busy = true,
                avatarUrl = preloadAvatar
            )
        }

        viewModelScope.launch {
            try {
                val calls = arrayListOf<Deferred<*>>()
                calls.add(async { librariesRepository.list() })
                if (selectedProfileId > 0) {
                    calls.add(async { profilesRepository.details(selectedProfileId) })
                }
                val results = calls.awaitAll()

                @Suppress("UNCHECKED_CAST")
                val libs = results[0] as List<BasicLibrary>
                allLibIds.addAll(libs.map { lib -> lib.id })

                if (selectedProfileId > 0) {

                    val detailedProfile = results[1] as DetailedProfile
                    val selfMode = selectedProfileId == authManager.currentProfileId
                    val selfAdminMode = selfMode && authManager.currentProfileIsMain


                    val selectedLibs =
                        if(selfAdminMode) {

                            // Main profile has access to all libs on account
                            libs.map { lib -> lib.id }
                        } else {

                            // Others require linked libraries
                            detailedProfile.availableLibraries.map { lib -> lib.id }
                        }

                    val availableLibs = libs.filter {
                        if(selfAdminMode) {

                            // Show all libs
                            true
                        } else if(selfMode) {

                            // selfMode but not admin, only show libs with current access
                            selectedLibs.contains(it.id)
                        } else {

                            // Editing someone else, show all libs
                            true
                        }
                    }

                    _uiState.update {
                        it.copy(
                            loadingComplete = true,
                            name = detailedProfile.name,
                            hasPin = detailedProfile.hasPin,
                            lockedState = if(detailedProfile.locked) LockedState.Locked else LockedState.Unlocked,
                            avatarUrl = detailedProfile.avatarUrl,
                            libraries = availableLibs,
                            selectedLibraryIds = selectedLibs,
                            maxMovieRating = detailedProfile.maxMovieRating,
                            maxTVRating = detailedProfile.maxTVRating,
                            titleRequestPermissions = detailedProfile.titleRequestPermissions,
                            addMode = false,
                            selfMode = selfMode
                        )
                    }
                } else {
                    allLibIds.addAll(libs.map { lib -> lib.id })
                    _uiState.update {
                        it.copy(
                            loadingComplete = true,
                            libraries = libs,
                            addMode = true,
                            selfMode = false
                        )
                    }
                }
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = true)
            }
        }
    }

    fun infoLoaded() {
        _uiState.update {
            it.copy(
                busy = false,
                loadingComplete = false
            )
        }
    }


    fun setError(ex: Exception, criticalError: Boolean) {
        ex.logToCrashlytics()
        ex.printStackTrace()
        _uiState.update {
            it.copy(
                busy = false,
                showErrorDialog = true,
                errorMessage = ex.localizedMessage,
                criticalError = criticalError
            )
        }
    }

    fun hideError() {
        if (_uiState.value.criticalError) {
            popBackStack()
        } else {
            _uiState.update {
                it.copy(showErrorDialog = false)
            }
        }
    }

    fun saveProfile(
        name: String,
        pin: String,
        deletePin: Boolean,
        maxMovieRating: MovieRatings,
        maxTVRating: TVRatings,
        titleRequestPermissions: TitleRequestPermissions,
        lockedState: LockedState,
        selectedLibs: List<Int>,
        avatarFile: String
    ) {
        _uiState.update {
            it.copy(busy = true)
        }
        if (selectedProfileId == 0) {
            saveNewProfile(
                name = name,
                pin = pin,
                maxMovieRating = maxMovieRating,
                maxTVRating = maxTVRating,
                titleRequestPermissions = titleRequestPermissions,
                selectedLibs = selectedLibs,
                avatarFile = avatarFile
            )
        } else {
            updateProfile(
                name = name,
                pin = pin,
                deletePin = deletePin,
                maxMovieRating = maxMovieRating,
                maxTVRating = maxTVRating,
                titleRequestPermissions = titleRequestPermissions,
                lockedState = lockedState,
                selectedLibs = selectedLibs,
                avatarFile = avatarFile
            )
        }
    }


    private fun saveNewProfile(
        name: String,
        pin: String,
        maxMovieRating: MovieRatings,
        maxTVRating: TVRatings,
        titleRequestPermissions: TitleRequestPermissions,
        selectedLibs: List<Int>,
        avatarFile: String
    ) {
        viewModelScope.launch {
            var pop = false
            try {

                //Create the profile
                val createProfile = CreateProfile(
                    name = name,
                    pin = pin.toUShortOrNull()?.toInt(),
                    locked = false,
                    avatarUrl = preloadAvatar,
                    maxMovieRating = maxMovieRating,
                    maxTVRating = maxTVRating,
                    titleRequestPermissions = titleRequestPermissions
                )
                val profileId = profilesRepository.create(createProfile)


                //Share libs
                val secondaryErrors = arrayListOf<String>()
                try {
                    val calls = arrayListOf<Deferred<*>>()
                    for (libraryId in selectedLibs) {
                        calls.add(
                            async {
                                profilesRepository.linkToLibrary(
                                    ProfileLibraryLink(
                                        profileId = profileId,
                                        libraryId = libraryId
                                    )
                                )
                            }
                        )
                    }
                    calls.awaitAll()
                } catch (_: Exception) {
                    secondaryErrors.add("Errors setting library access")
                }

                //Set avatar
                try {
                    if (avatarFile.isNotBlank() && !avatarFile.startsWith("https://", ignoreCase = true)) {
                        val maxSize = 1024 * 1024
                        var data = File(avatarFile).readBytes()
                        var smallEnough = data.size <= maxSize && data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte()
                        if (!smallEnough) {
                            val bitmap = BitmapFactory.decodeFile(avatarFile)
                            var quality = 100
                            while (!smallEnough) {
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                                if (stream.size() <= maxSize) {
                                    data = stream.toByteArray()
                                    smallEnough = true
                                } else {
                                    quality -= 5
                                    if (quality <= 0)
                                        throw Exception("Could not shrink image to less than 1 MB. Please choose a different image.")
                                }
                            }
                        }
                        profilesRepository.setAvatar(profileId, data.toRequestBody())
                    }
                } catch (_: Exception) {
                    secondaryErrors.add("Error setting avatar")
                }

                //Update previous screen
                ProfilesSettingsViewModel.triggerUpdate()

                if (secondaryErrors.isEmpty()) {
                    popBackStack()
                } else {
                    pop = true
                    var msg = "The profile was created, but the following errors occurred:\n"
                    for (ex in secondaryErrors)
                        msg += "\n - $ex"
                    throw Exception(msg)
                }


            } catch (ex: Exception) {
                setError(ex = ex, criticalError = pop)
            }
        }
    }


    private fun updateProfile(
        name: String,
        pin: String,
        deletePin: Boolean,
        maxMovieRating: MovieRatings,
        maxTVRating: TVRatings,
        titleRequestPermissions: TitleRequestPermissions,
        lockedState: LockedState,
        selectedLibs: List<Int>,
        avatarFile: String
    ) {

        viewModelScope.launch {
            var pop = false
            try {

                val selfAdminMode = selectedProfileId == authManager.currentProfileId && authManager.currentProfileIsMain

                val updateProfile = UpdateProfile(
                    id = selectedProfileId,
                    name = name,
                    pin = pin.toUShortOrNull()?.toInt(),
                    clearPin = deletePin,
                    avatarUrl = _uiState.value.avatarUrl,

                    //server will ignore these 4 if in self mode
                    locked = lockedState == LockedState.Locked,
                    maxMovieRating = maxMovieRating,
                    maxTVRating = maxTVRating,
                    titleRequestPermissions = titleRequestPermissions
                )

                profilesRepository.update(updateProfile)

                val secondaryErrors = arrayListOf<String>()

                //Server knows better for main profile library sharing, but save time and skip anyway
                if(!selfAdminMode) {
                    try {

                        //Remove libs no longer allowed
                        val calls = arrayListOf<Deferred<*>>()
                        for (libId in allLibIds) {
                            if (uiState.value.selectedLibraryIds.contains(libId)) {
                                if (!selectedLibs.contains(libId)) {
                                    calls.add(
                                        async {
                                            profilesRepository.unLinkFromLibrary(
                                                ProfileLibraryLink(
                                                    profileId = selectedProfileId,
                                                    libraryId = libId
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        //Add newly allowed libs
                        for (libId in selectedLibs) {
                            if (!uiState.value.selectedLibraryIds.contains(libId)) {
                                calls.add(
                                    async {
                                        profilesRepository.linkToLibrary(
                                            ProfileLibraryLink(
                                                profileId = selectedProfileId,
                                                libraryId = libId
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        calls.awaitAll()
                    } catch (ex: Exception) {
                        secondaryErrors.add("Errors setting library access")
                    }
                }

                //Set avatar
                try {
                    if (avatarFile.isNotBlank() && !avatarFile.startsWith("https://", ignoreCase = true)) {
                        val maxSize = 1024 * 1024
                        var data = File(avatarFile).readBytes()
                        var smallEnough = data.size <= maxSize && data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte()
                        if (!smallEnough) {
                            val bitmap = BitmapFactory.decodeFile(avatarFile)
                            var quality = 100
                            while (!smallEnough) {
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                                if (stream.size() <= maxSize) {
                                    data = stream.toByteArray()
                                    smallEnough = true
                                } else {
                                    quality -= 5
                                    if (quality <= 0)
                                        throw Exception("Could not shrink image to less than 1 MB. Please choose a different image.")
                                }
                            }
                        }
                        profilesRepository.setAvatar(selectedProfileId, data.toRequestBody())
                    }
                } catch (_: Exception) {
                    secondaryErrors.add("Error setting avatar")
                }


                //Update previous screen
                ProfilesSettingsViewModel.triggerUpdate()

                if (secondaryErrors.isEmpty()) {
                    popBackStack()
                } else {
                    pop = true
                    var msg = "The profile was created, but the following errors occurred:\n"
                    for (ex in secondaryErrors)
                        msg += "\n - $ex"
                    throw Exception(msg)
                }

            } catch (ex: Exception) {
                setError(ex = ex, criticalError = pop)
            }
        }
    }

    fun deleteProfile() {
        _uiState.update {
            it.copy(busy = true)
        }
        viewModelScope.launch {
            try {
                profilesRepository.delete(selectedProfileId)
                ProfilesSettingsViewModel.triggerUpdate()
                popBackStack()
            } catch (ex: Exception) {
                setError(ex = ex, criticalError = false)
            }
        }
    }

}





















