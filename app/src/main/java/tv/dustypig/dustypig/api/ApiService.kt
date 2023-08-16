package tv.dustypig.dustypig.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.models.CreateAccountResponse
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.HomeScreen
import tv.dustypig.dustypig.api.models.LoadMoreHomeScreenItemsRequest
import tv.dustypig.dustypig.api.models.LoginResponse
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.models.ResponseWrapper
import tv.dustypig.dustypig.api.models.ResponseWrapperOf
import tv.dustypig.dustypig.api.models.SimpleValue

interface ApiService {


    // ***** Account *****
    @POST("Account/Create")
    suspend fun createAccount(@Body createAccount: CreateAccount): Response<ResponseWrapperOf<CreateAccountResponse>>



    // ***** Auth *****
    @POST("Auth/PasswordLogin")
    suspend fun passwordLogin(@Body passwordCredentials: PasswordCredentials): Response<ResponseWrapperOf<LoginResponse>>

    @POST("Auth/SendPasswordResetEmail")
    suspend fun sendPasswordResetEmail(@Body email: SimpleValue<String>): Response<ResponseWrapper>

    @POST("Auth/ProfileLogin")
    suspend fun profileLogin(@Body profileCredentials: ProfileCredentials): Response<ResponseWrapperOf<LoginResponse>>



    // ***** Media *****
    @GET("Media/HomeScreen")
    suspend fun homeScreen(): Response<ResponseWrapperOf<HomeScreen>>

    @POST("Media/LoadMoreHomeScreenItems")
    suspend fun loadMoreHomeScreenItems(@Body loadMoreHomeScreenListItemsRequest: LoadMoreHomeScreenItemsRequest): Response<ResponseWrapperOf<List<BasicMedia>>>



    // *** Movies ***
    @GET("Movies/Details/{id}")
    suspend fun movieDetails(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedMovie>>


    // ***** Profiles *****
    @GET("Profiles/List")
    suspend fun listProfiles(): Response<ResponseWrapperOf<List<BasicProfile>>>


}
