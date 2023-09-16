package tv.dustypig.dustypig.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import tv.dustypig.dustypig.api.models.AddPlaylistItem
import tv.dustypig.dustypig.api.models.BasicFriend
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicPlaylist
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.models.CreateAccountResponse
import tv.dustypig.dustypig.api.models.CreatePlaylist
import tv.dustypig.dustypig.api.models.DetailedEpisode
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.DetailedTMDB
import tv.dustypig.dustypig.api.models.HomeScreen
import tv.dustypig.dustypig.api.models.LoadMoreHomeScreenItemsRequest
import tv.dustypig.dustypig.api.models.LoginResponse
import tv.dustypig.dustypig.api.models.MovePlaylistItem
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.models.ResponseWrapper
import tv.dustypig.dustypig.api.models.ResponseWrapperOf
import tv.dustypig.dustypig.api.models.SearchRequest
import tv.dustypig.dustypig.api.models.SearchResults
import tv.dustypig.dustypig.api.models.SimpleValue
import tv.dustypig.dustypig.api.models.TitlePermissionInfo
import tv.dustypig.dustypig.api.models.TitleRequest
import tv.dustypig.dustypig.api.models.UpdatesPlaylist

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




    // ***** Episodes *****
    @GET("Episodes/Details/{id}")
    suspend fun episodeDetails(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedEpisode>>




    // ***** Friends *****
    @GET("Friends/List")
    suspend fun listFriends(): Response<ResponseWrapperOf<List<BasicFriend>>>





    // ***** Media *****
    @GET("Media/AddToWatchlist/{id}")
    suspend fun addToWatchlist(@Path("id") id: Int): Response<ResponseWrapper>

    @DELETE("Media/DeleteFromWatchlist/{id}")
    suspend fun deleteFromWatchlist(@Path("id") id: Int): Response<ResponseWrapper>

    @GET("Media/GetTitlePermissions/{id}")
    suspend fun getTitlePermissions(@Path("id") id: Int): Response<ResponseWrapperOf<TitlePermissionInfo>>

    @GET("Media/HomeScreen")
    suspend fun homeScreen(): Response<ResponseWrapperOf<HomeScreen>>

    @POST("Media/LoadMoreHomeScreenItems")
    suspend fun loadMoreHomeScreenItems(@Body loadMoreHomeScreenListItemsRequest: LoadMoreHomeScreenItemsRequest): Response<ResponseWrapperOf<List<BasicMedia>>>

    @GET("Media/RequestAccessOverride/{id}")
    suspend fun requestAccessOverride(@Path("id") id: Int): Response<ResponseWrapper>

    @POST("Media/Search")
    suspend fun search(@Body searchRequest: SearchRequest): Response<ResponseWrapperOf<SearchResults>>

    @POST("Media/SetTitlePermissions")
    suspend fun setTitlePermissions(@Body titlePermissionInfo: TitlePermissionInfo) : Response<ResponseWrapper>

    @POST("Media/UpdatePlaybackProgress")
    suspend fun updatePlaybackProgress(@Body playbackProgress: PlaybackProgress): Response<ResponseWrapper>



    // *** Movies ***
    @GET("Movies/Details/{id}")
    suspend fun movieDetails(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedMovie>>




    // ***** Playlists *****
    @POST("Playlists/AddItem")
    suspend fun addItemToPlaylist(@Body addPlaylistItem: AddPlaylistItem): Response<ResponseWrapperOf<SimpleValue<Int>>>

    @POST("Playlists/AddSeries")
    suspend fun addSeriesToPlaylist(@Body addPlaylistItem: AddPlaylistItem): Response<ResponseWrapper>

    @POST("Playlists/Create")
    suspend fun createPlaylist(@Body createPlaylist: CreatePlaylist): Response<ResponseWrapperOf<SimpleValue<Int>>>

    @DELETE("Playlists/Delete/{id}")
    suspend fun deletePlaylist(@Path("id") id: Int): Response<ResponseWrapper>

    @DELETE("Playlists/DeleteItem/{id}")
    suspend fun deletePlaylistItem(@Path("id") id: Int): Response<ResponseWrapper>

    @GET("Playlists/List")
    suspend fun listPlaylists(): Response<ResponseWrapperOf<List<BasicPlaylist>>>

    @POST("Playlists/Update")
    suspend fun updatePlaylist(@Body updatesPlaylist: UpdatesPlaylist): Response<ResponseWrapper>

    @GET("Playlists/Details/{id}")
    suspend fun playlistDetails(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedPlaylist>>

    @POST("Playlists/MoveItemToNewIndex")
    suspend fun movePlaylistItemToNewIndex(@Body movePlaylistItem: MovePlaylistItem): Response<ResponseWrapper>





    // ***** Profiles *****
    @GET("Profiles/List")
    suspend fun listProfiles(): Response<ResponseWrapperOf<List<BasicProfile>>>


    // ***** Series *****
    @GET("Series/Details/{id}")
    suspend fun seriesDetails(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedSeries>>

    @GET("Series/MarkSeriesWatched/{id}")
    suspend fun markSeriesWatched(@Path("id") id: Int): Response<ResponseWrapper>

    @GET("Series/RemoveFromContinueWatching/{id}")
    suspend fun removeFromContinueWatching(@Path("id") id: Int): Response<ResponseWrapper>



    // ***** TMDB *****
    @GET("TMDB/GetMovie/{id}")
    suspend fun getTMDBMovie(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedTMDB>>

    @GET("TMDB/GetSeries/{id}")
    suspend fun getTMDBSeries(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedTMDB>>

    @POST("TMDB/RequestTitle")
    suspend fun requestTMDBTitle(@Body titleRequest: TitleRequest): Response<ResponseWrapper>

    @POST("TMDB/CancelTitleRequest")
    suspend fun cancelTMDBTitleRequest(@Body titleRequest: TitleRequest): Response<ResponseWrapper>
}
