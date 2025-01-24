package tv.dustypig.dustypig.api.repositories

import okhttp3.MultipartBody
import okhttp3.RequestBody
import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.CreateProfile
import tv.dustypig.dustypig.api.models.ProfileLibraryLink
import tv.dustypig.dustypig.api.models.UpdateProfile
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.AuthManager
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ProfilesRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
) : RepositoryBase(authManager) {

    suspend fun list() = wrapAPICallWithReturnData { apiService.listProfiles() }

    suspend fun details(id: Int) = wrapAPICallWithReturnData { apiService.profileDetails(id) }

    suspend fun delete(id: Int) = wrapAPICall { apiService.deleteProfile(id) }

    suspend fun create(createProfile: CreateProfile) =
        wrapAPICallWithReturnData { apiService.createProfile(createProfile) }

    suspend fun update(updateProfile: UpdateProfile) =
        wrapAPICall { apiService.updateProfile(updateProfile) }

    suspend fun linkToLibrary(profileLibraryLink: ProfileLibraryLink) =
        wrapAPICall { apiService.linkProfileLibrary(profileLibraryLink) }

    suspend fun unLinkFromLibrary(profileLibraryLink: ProfileLibraryLink) =
        wrapAPICall { apiService.unLinkProfileLibrary(profileLibraryLink) }

    suspend fun setAvatar(id: Int, requestBody: RequestBody) = wrapAPICallWithReturnData {
        apiService.setProfileAvatar(
            id = id,
            image = MultipartBody.Part.createFormData(
                name = "image",
                filename = "image.jpg",
                body = requestBody
            )
        )
    }
}
