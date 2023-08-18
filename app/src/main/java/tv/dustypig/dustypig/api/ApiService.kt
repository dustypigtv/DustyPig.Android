package tv.dustypig.dustypig.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.models.CreateAccountResponse
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.HomeScreen
import tv.dustypig.dustypig.api.models.LoadMoreHomeScreenItemsRequest
import tv.dustypig.dustypig.api.models.LoginResponse
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.models.PlaybackProgress
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
    @GET("Media/AddToWatchlist/{id}")
    suspend fun addToWatchlist(@Path("id") id: Int): Response<ResponseWrapper>

    @DELETE("Media/DeleteFromWatchlist/{id}")
    suspend fun deleteFromWatchlist(@Path("id") id: Int): Response<ResponseWrapper>

    @GET("Media/HomeScreen")
    suspend fun homeScreen(): Response<ResponseWrapperOf<HomeScreen>>

    @POST("Media/LoadMoreHomeScreenItems")
    suspend fun loadMoreHomeScreenItems(@Body loadMoreHomeScreenListItemsRequest: LoadMoreHomeScreenItemsRequest): Response<ResponseWrapperOf<List<BasicMedia>>>

    @POST("Media/UpdatePlaybackProgress")
    suspend fun updatePlaybackProgress(@Body playbackProgress: PlaybackProgress): Response<ResponseWrapper>



    // *** Movies ***
    @GET("Movies/Details/{id}")
    suspend fun movieDetails(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedMovie>>


    // ***** Profiles *****
    @GET("Profiles/List")
    suspend fun listProfiles(): Response<ResponseWrapperOf<List<BasicProfile>>>


    // ***** Series *****
    @GET("Series/Details/{id}")
    suspend fun seriesDetails(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedSeries>>

}
