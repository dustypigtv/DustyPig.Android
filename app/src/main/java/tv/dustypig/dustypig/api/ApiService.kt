package tv.dustypig.dustypig.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import tv.dustypig.dustypig.AuthManager
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.models.CreateAccountResponse
import tv.dustypig.dustypig.api.models.HomeScreen
import tv.dustypig.dustypig.api.models.LoadMoreHomeScreenItemsRequest
import tv.dustypig.dustypig.api.models.LoginResponse
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.models.ResponseWrapper
import tv.dustypig.dustypig.api.models.ResponseWrapperOf
import tv.dustypig.dustypig.api.models.SimpleValue

interface ApiService {

    @Headers("Content-Type: application/json")

    private fun authHeaders(token:String): MutableMap<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "Bearer $token"
        return headerMap
    }

    // ***** Account *****
    @POST("Account/Create")
    suspend fun createAccount(@Body createAccount: CreateAccount): Response<ResponseWrapperOf<CreateAccountResponse>>



    // ***** Auth *****
    @POST("Auth/PasswordLogin")
    suspend fun passwordLogin(@Body passwordCredentials: PasswordCredentials): Response<ResponseWrapperOf<LoginResponse>>

    @POST("Auth/SendPasswordResetEmail")
    suspend fun sendPasswordResetEmail(@Body email: SimpleValue<String>): Response<ResponseWrapper>

    @POST("Auth/ProfileLogin")
    suspend fun profileLogin(@Body profileCredentials: ProfileCredentials,
                             @HeaderMap headers: Map<String, String> = authHeaders(AuthManager.currentToken)): Response<ResponseWrapperOf<LoginResponse>>



    // ***** Media *****
    @GET("Media/HomeScreen")
    suspend fun homeScreen(@HeaderMap headers: Map<String, String> = authHeaders(AuthManager.currentToken)): Response<ResponseWrapperOf<HomeScreen>>

    @POST("Media/LoadMoreHomeScreenItems")
    suspend fun loadMoreHomeScreenItems(@Body loadMoreHomeScreenListItemsRequest: LoadMoreHomeScreenItemsRequest,
                                        @HeaderMap headers: Map<String, String> = authHeaders(AuthManager.currentToken)): Response<ResponseWrapperOf<List<BasicMedia>>>



    // ***** Profiles *****
    @GET("Profiles/List")
    suspend fun listProfiles(@HeaderMap headers: Map<String, String> = authHeaders(AuthManager.currentToken)): Response<ResponseWrapperOf<List<BasicProfile>>>


}

object ThePig{

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://service.dustypig.tv/api/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}