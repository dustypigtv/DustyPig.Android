package tv.dustypig.dustypig.api.repositories

import okhttp3.MultipartBody
import okhttp3.RequestBody
import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.UpdateProfile
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import tv.dustypig.dustypig.global_managers.AuthManager
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ProfilesRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
): _RepositoryBase(authManager) {

    suspend fun list() = wrapAPICallWithReturnData { apiService.listProfiles() }

    suspend fun details(id: Int) = wrapAPICallWithReturnData { apiService.profileDetails(id) }

    suspend fun update(updateProfile: UpdateProfile) = wrapAPICall { apiService.updateProfile(updateProfile) }

    suspend fun setAvatar(id: Int, requestBody: RequestBody) = wrapAPICallWithReturnSimpleValue {
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
