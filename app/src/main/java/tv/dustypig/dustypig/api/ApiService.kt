package tv.dustypig.dustypig.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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
import tv.dustypig.dustypig.api.models.DetailedFriend
import tv.dustypig.dustypig.api.models.DetailedLibrary
import tv.dustypig.dustypig.api.models.DetailedMovie
import tv.dustypig.dustypig.api.models.DetailedPlaylist
import tv.dustypig.dustypig.api.models.DetailedProfile
import tv.dustypig.dustypig.api.models.DetailedSeries
import tv.dustypig.dustypig.api.models.DetailedTMDB
import tv.dustypig.dustypig.api.models.HomeScreen
import tv.dustypig.dustypig.api.models.LibraryFriendLink
import tv.dustypig.dustypig.api.models.LoadMoreHomeScreenItemsRequest
import tv.dustypig.dustypig.api.models.LoginResponse
import tv.dustypig.dustypig.api.models.MovePlaylistItem
import tv.dustypig.dustypig.api.models.Notification
import tv.dustypig.dustypig.api.models.PasswordCredentials
import tv.dustypig.dustypig.api.models.PlaybackProgress
import tv.dustypig.dustypig.api.models.ProfileCredentials
import tv.dustypig.dustypig.api.models.ResponseWrapper
import tv.dustypig.dustypig.api.models.ResponseWrapperOf
import tv.dustypig.dustypig.api.models.SearchRequest
import tv.dustypig.dustypig.api.models.SearchResults
import tv.dustypig.dustypig.api.models.SetTitlePermissionInfo
import tv.dustypig.dustypig.api.models.SimpleValue
import tv.dustypig.dustypig.api.models.TitlePermissionInfo
import tv.dustypig.dustypig.api.models.TitleRequest
import tv.dustypig.dustypig.api.models.UpdateFriend
import tv.dustypig.dustypig.api.models.UpdateProfile
import tv.dustypig.dustypig.api.models.UpdatesPlaylist

interface ApiService {

    // ***** Account *****
    @POST("Account/Create")
    suspend fun createAccount(@Body createAccount: CreateAccount): Response<ResponseWrapperOf<CreateAccountResponse>>

    @POST("Account/ChangePassword")
    suspend fun changePassword(@Body newPassword: SimpleValue<String>): Response<ResponseWrapper>

    @DELETE("Account/Delete")
    suspend fun deleteAccount(): Response<ResponseWrapper>




    // ***** Auth *****
    @POST("Auth/PasswordLogin")
    suspend fun passwordLogin(@Body passwordCredentials: PasswordCredentials): Response<ResponseWrapperOf<LoginResponse>>

    @POST("Auth/SendPasswordResetEmail")
    suspend fun sendPasswordResetEmail(@Body email: SimpleValue<String>): Response<ResponseWrapper>

    @POST("Auth/ProfileLogin")
    suspend fun profileLogin(@Body profileCredentials: ProfileCredentials): Response<ResponseWrapperOf<LoginResponse>>

    @POST("Auth/LoginDeviceWithCode")
    suspend fun loginDeviceWithCode(@Body deviceCode: SimpleValue<String>): Response<ResponseWrapper>

    @GET("Auth/Signout")
    suspend fun signout(): Response<ResponseWrapper>

    @GET("Auth/SignoutEverywhere")
    suspend fun signoutEverywhere(): Response<ResponseWrapper>







    // ***** Episodes *****
    @GET("Episodes/Details/{id}")
    suspend fun episodeDetails(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedEpisode>>




    // ***** Friends *****
    @GET("Friends/List")
    suspend fun listFriends(): Response<ResponseWrapperOf<List<BasicFriend>>>

    @POST("Friends/Invite")
    suspend fun inviteFriend(@Body email: SimpleValue<String>): Response<ResponseWrapper>

    @GET("Friends/Details/{id}")
    suspend fun friendDetails(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedFriend>>

    @POST("Friends/Update")
    suspend fun updateFriend(@Body updateFriend: UpdateFriend): Response<ResponseWrapper>

    @POST("Friends/ShareLibrary")
    suspend fun shareLibraryWithFriend(@Body libraryFriendLink: LibraryFriendLink): Response<ResponseWrapper>

    @POST("Friends/UnShareLibrary")
    suspend fun unShareLibraryWithFriend(@Body libraryFriendLink: LibraryFriendLink): Response<ResponseWrapper>







    // ***** Libraries *****
    @GET("Libraries/AdminList")
    suspend fun adminListLibraries(): Response<ResponseWrapperOf<List<DetailedLibrary>>>



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
    suspend fun setTitlePermissions(@Body setTitlePermissionInfo: SetTitlePermissionInfo) : Response<ResponseWrapper>

    @POST("Media/UpdatePlaybackProgress")
    suspend fun updatePlaybackProgress(@Body playbackProgress: PlaybackProgress): Response<ResponseWrapper>



    // ***** Movies *****
    @GET("Movies/Details/{id}")
    suspend fun movieDetails(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedMovie>>


    // ***** Notifications *****
    @DELETE("Notifications/Delete/{id}")
    suspend fun deleteNotification(@Path("id") id: Int): Response<ResponseWrapper>

    @GET("Notifications/List")
    suspend fun listNotifications(): Response<ResponseWrapperOf<List<Notification>>>

    @GET("Notifications/MarkAsRead/{id}")
    suspend fun markNotificationRead(@Path("id") id: Int): Response<ResponseWrapper>



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

    @GET("Profiles/Details/{id}")
    suspend fun profileDetails(@Path("id") id: Int): Response<ResponseWrapperOf<DetailedProfile>>

    @POST("Profiles/Update")
    suspend fun updateProfile(@Body updateProfile: UpdateProfile): Response<ResponseWrapper>

    @Multipart
    @PUT("Profiles/SetProfileAvatarMultipart/{id}")
    suspend fun setProfileAvatar(@Path("id") id: Int, @Part image: MultipartBody.Part): Response<ResponseWrapperOf<SimpleValue<String>>>



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
