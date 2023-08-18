package tv.dustypig.dustypig.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tv.dustypig.dustypig.AuthManager
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.models.CreateAccountResponse
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.HomeScreen
import tv.dustypig.dustypig.api.models.HomeScreenList
import tv.dustypig.dustypig.api.models.LoadMoreHomeScreenItemsRequest
import tv.dustypig.dustypig.api.models.LoginResponse
import tv.dustypig.dustypig.api.models.MediaTypes
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.models.ResponseWrapper
import tv.dustypig.dustypig.api.models.ResponseWrapperOf
import tv.dustypig.dustypig.api.models.SimpleValue
import java.io.IOException

object ThePig{

    /**
     * Temporarily stores data for the ShowMore screen, reducing network calls
      */
    var showMoreData: HomeScreenList = HomeScreenList(0, "", listOf())

    /**
     * Temporarily stores data for details screens
      */
    var selectedBasicMedia: BasicMedia = BasicMedia(0, MediaTypes.Movie, "", "")


    /**
     * Singleton for API Calls
     */
    object Api {

        private const val baseUrl = "https://service.dustypig.tv/api/v3/"

        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient()
                .newBuilder()
                .addInterceptor(AuthorizationInterceptor)
                .build()
        }

        private val gsonConverterFactory: GsonConverterFactory by lazy {
            GsonConverterFactory.create(GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create())
        }

        private val authenticatedApi: ApiService by lazy {
            Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(ApiService::class.java)
        }

        private val unauthenticatedApi: ApiService by lazy {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(ApiService::class.java)
        }

        private suspend fun wrapAPICall(call: suspend () -> (Response<ResponseWrapper>)) {
            try{
                val response = call.invoke()
                if(!response.isSuccessful) {
                    if(response.code() == 401) {
                        AuthManager.setAuthState("", 0, false)
                        return
                    } else {
                        throw Exception(response.message())
                    }
                }
                val rw = response.body()!!
                if(!rw.success)
                    throw Exception(rw.error)
            }
            catch (ex: IOException) {
                throw Exception("Not connected to the internet")
            }
            catch (ex: Exception) {
                if(ex.localizedMessage.isNullOrBlank()) {
                    throw Exception("Unknown Error")
                }
                throw ex
            }
        }

        private suspend fun <T> wrapAPICallWithReturnData(call: suspend () -> (Response<ResponseWrapperOf<T>>)): T {

            try {
                val response = call.invoke()
                if (!response.isSuccessful) {
                    if (response.code() == 401) {
                        AuthManager.logout()
                        throw Exception(response.message())
                    } else {
                        throw Exception(response.message())
                    }
                }
                val rw = response.body()!!
                if (!rw.success) {
                    throw Exception(rw.error)
                }
                return response.body()!!.data!!
            }
            catch (ex: IOException) {
                throw Exception("Not connected to the internet")
            }
            catch (ex: Exception) {
                if (ex.localizedMessage.isNullOrBlank()) {
                    throw Exception("Unknown Error")
                }
                throw ex
            }
        }




        object Account {
            suspend fun createAccount(createAccount: CreateAccount) : CreateAccountResponse =
                wrapAPICallWithReturnData { unauthenticatedApi.createAccount(createAccount) }
        }

        object Auth {
            suspend fun passwordLogin(passwordCredentials: PasswordCredentials) : LoginResponse =
                wrapAPICallWithReturnData{ unauthenticatedApi.passwordLogin(passwordCredentials) }

            suspend fun sendPasswordResetEmail(email: String) =
                wrapAPICall { unauthenticatedApi.sendPasswordResetEmail(SimpleValue(email)) }

            suspend fun profileLogin(profileCredentials: ProfileCredentials) : LoginResponse =
                wrapAPICallWithReturnData { authenticatedApi.profileLogin(profileCredentials) }
        }



        object Media {

            suspend fun addToWatchlist(id: Int) = wrapAPICall { authenticatedApi.addToWatchlist(id) }

            suspend fun deleteFromWatchlist(id: Int) = wrapAPICall { authenticatedApi.deleteFromWatchlist(id) }

            suspend fun homeScreen(): HomeScreen = wrapAPICallWithReturnData { authenticatedApi.homeScreen() }

            suspend fun loadMoreHomeScreenItems(loadMoreHomeScreenListItemsRequest: LoadMoreHomeScreenItemsRequest): List<BasicMedia> =
                wrapAPICallWithReturnData { authenticatedApi.loadMoreHomeScreenItems(loadMoreHomeScreenListItemsRequest) }

            suspend fun updatePlaybackProgress(id: Int, seconds: Double) = wrapAPICall { authenticatedApi.updatePlaybackProgress(PlaybackProgress(id, seconds)) }
        }


        object Movies {
            suspend fun movieDetails(id: Int) : DetailedMovie = wrapAPICallWithReturnData { authenticatedApi.movieDetails(id) }
        }



        object Profiles {
            suspend fun listProfiles() : List<BasicProfile> = wrapAPICallWithReturnData { authenticatedApi.listProfiles() }
        }


        object Series {
            suspend fun seriesDetails(id: Int) : DetailedSeries = wrapAPICallWithReturnData { authenticatedApi.seriesDetails(id) }
        }
    }




}