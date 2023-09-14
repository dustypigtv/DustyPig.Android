package tv.dustypig.dustypig

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.AuthorizationInterceptor
import tv.dustypig.dustypig.api.models.AddPlaylistItem
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.models.CreatePlaylist
import tv.dustypig.dustypig.api.models.HomeScreen
import tv.dustypig.dustypig.api.models.HomeScreenList
import tv.dustypig.dustypig.api.models.LoadMoreHomeScreenItemsRequest
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.models.ResponseWrapper
import tv.dustypig.dustypig.api.models.ResponseWrapperOf
import tv.dustypig.dustypig.api.models.SimpleValue
import tv.dustypig.dustypig.api.models.TitlePermissionInfo
import java.io.IOException

/**
 * Global States
 */
object ThePig{



    /**
     * Temporarily stores data for the ShowMore screen, reducing network calls
      */
    var showMoreData: HomeScreenList = HomeScreenList(0, "", listOf())








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

        private suspend fun <T> wrapAPICallWithReturnSimpleValue(call: suspend () -> (Response<ResponseWrapperOf<SimpleValue<T>>>)): T {

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
                return response.body()!!.data!!.value
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
            suspend fun createAccount(createAccount: CreateAccount) =
                wrapAPICallWithReturnData { unauthenticatedApi.createAccount(createAccount) }
        }

        object Auth {
            suspend fun passwordLogin(passwordCredentials: PasswordCredentials) =
                wrapAPICallWithReturnData{ unauthenticatedApi.passwordLogin(passwordCredentials) }

            suspend fun sendPasswordResetEmail(email: String) =
                wrapAPICall { unauthenticatedApi.sendPasswordResetEmail(SimpleValue(email)) }

            suspend fun profileLogin(profileCredentials: ProfileCredentials) =
                wrapAPICallWithReturnData { authenticatedApi.profileLogin(profileCredentials) }
        }

        object Episodes {
            suspend fun episodeDetails(id: Int) = wrapAPICallWithReturnData { authenticatedApi.episodeDetails(id) }
        }



        object Media {

            suspend fun addToWatchlist(id: Int) = wrapAPICall { authenticatedApi.addToWatchlist(id) }

            suspend fun deleteFromWatchlist(id: Int) = wrapAPICall { authenticatedApi.deleteFromWatchlist(id) }

            suspend fun getTitlePermissions(id: Int) = wrapAPICallWithReturnData { authenticatedApi.getTitlePermissions(id) }

            suspend fun homeScreen(): HomeScreen = wrapAPICallWithReturnData { authenticatedApi.homeScreen() }

            suspend fun loadMoreHomeScreenItems(loadMoreHomeScreenListItemsRequest: LoadMoreHomeScreenItemsRequest) =
                wrapAPICallWithReturnData { authenticatedApi.loadMoreHomeScreenItems(loadMoreHomeScreenListItemsRequest) }

            suspend fun requestAccessOverride (id: Int) = wrapAPICall { authenticatedApi.requestAccessOverride(id) }

            suspend fun setTitlePermissions(titlePermissionInfo: TitlePermissionInfo) = wrapAPICall { authenticatedApi.setTitlePermissions(titlePermissionInfo) }

            suspend fun updatePlaybackProgress(id: Int, seconds: Double) = wrapAPICall { authenticatedApi.updatePlaybackProgress(PlaybackProgress(id, seconds)) }
        }


        object Movies {
            suspend fun movieDetails(id: Int) = wrapAPICallWithReturnData { authenticatedApi.movieDetails(id) }
        }


        object Playlists {

            suspend fun addItemToPlaylist(addPlaylistItem: AddPlaylistItem) = wrapAPICallWithReturnSimpleValue { authenticatedApi.addItemToPlaylist(addPlaylistItem) }

            suspend fun addSeriesToPlaylist(addPlaylistItem: AddPlaylistItem) = wrapAPICall { authenticatedApi.addSeriesToPlaylist(addPlaylistItem) }

            suspend fun createPlaylist(createPlaylist: CreatePlaylist) = wrapAPICallWithReturnSimpleValue { authenticatedApi.createPlaylist(createPlaylist) }

            suspend fun playlistDetails(id: Int) = wrapAPICallWithReturnData { authenticatedApi.playlistDetails(id) }

            suspend fun listPlaylists() = wrapAPICallWithReturnData { authenticatedApi.listPlaylists() }
        }



        object Profiles {
            suspend fun listProfiles() = wrapAPICallWithReturnData { authenticatedApi.listProfiles() }
        }


        object Series {
            suspend fun seriesDetails(id: Int) = wrapAPICallWithReturnData { authenticatedApi.seriesDetails(id) }

            suspend fun markSeriesWatched(id: Int) = wrapAPICall { authenticatedApi.markSeriesWatched(id) }

            suspend fun removeFromContinueWatching(id: Int) = wrapAPICall { authenticatedApi.removeFromContinueWatching(id) }
        }
    }




}