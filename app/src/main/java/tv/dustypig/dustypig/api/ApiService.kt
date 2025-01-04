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
import tv.dustypig.dustypig.api.models.AddSeriesToPlaylistInfo
import tv.dustypig.dustypig.api.models.BasicFriend
import tv.dustypig.dustypig.api.models.BasicLibrary
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.BasicPlaylist
import tv.dustypig.dustypig.api.models.BasicProfile
import tv.dustypig.dustypig.api.models.CreateAccount
import tv.dustypig.dustypig.api.models.CreatePlaylist
import tv.dustypig.dustypig.api.models.CreateProfile
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
import tv.dustypig.dustypig.api.models.ProfileLibraryLink
import tv.dustypig.dustypig.api.models.Result
import tv.dustypig.dustypig.api.models.ResultOf
import tv.dustypig.dustypig.api.models.SearchRequest
import tv.dustypig.dustypig.api.models.SearchResults
import tv.dustypig.dustypig.api.models.SetTitlePermission
import tv.dustypig.dustypig.api.models.TMDB_Person
import tv.dustypig.dustypig.api.models.TitlePermissions
import tv.dustypig.dustypig.api.models.TitleRequest
import tv.dustypig.dustypig.api.models.UpdateFriend
import tv.dustypig.dustypig.api.models.UpdateProfile
import tv.dustypig.dustypig.api.models.UpdatesPlaylist

interface ApiService {

    // ***** Account *****
    @POST("Account/Create")
    suspend fun createAccount(@Body createAccount: CreateAccount): Response<Result>

    @POST("Account/ChangePassword")
    suspend fun changePassword(@Body newPassword: String): Response<Result>

    @DELETE("Account/Delete")
    suspend fun deleteAccount(): Response<Result>




    // ***** Auth *****
    @POST("Auth/PasswordLogin")
    suspend fun passwordLogin(@Body passwordCredentials: PasswordCredentials): Response<ResultOf<LoginResponse>>

    @POST("Auth/SendPasswordResetEmail")
    suspend fun sendPasswordResetEmail(@Body email: String): Response<Result>

    @POST("Auth/ProfileLogin")
    suspend fun profileLogin(@Body profileCredentials: ProfileCredentials): Response<ResultOf<LoginResponse>>

    @POST("Auth/LoginDeviceWithCode")
    suspend fun loginDeviceWithCode(@Body deviceCode: String): Response<Result>

    @GET("Auth/Signout")
    suspend fun signout(): Response<Result>

    @GET("Auth/SignoutEverywhere")
    suspend fun signoutEverywhere(): Response<Result>







    // ***** Episodes *****
    @GET("Episodes/Details/{id}")
    suspend fun episodeDetails(@Path("id") id: Int): Response<ResultOf<DetailedEpisode>>




    // ***** Friends *****
    @GET("Friends/List")
    suspend fun listFriends(): Response<ResultOf<List<BasicFriend>>>

    @POST("Friends/Invite")
    suspend fun inviteFriend(@Body email: String): Response<Result>

    @GET("Friends/Details/{id}")
    suspend fun friendDetails(@Path("id") id: Int): Response<ResultOf<DetailedFriend>>

    @POST("Friends/Update")
    suspend fun updateFriend(@Body updateFriend: UpdateFriend): Response<Result>

    @DELETE("Friends/Unfriend/{id}")
    suspend fun unfriend(@Path("id") id: Int): Response<Result>

    @POST("Friends/ShareLibrary")
    suspend fun shareLibraryWithFriend(@Body libraryFriendLink: LibraryFriendLink): Response<Result>

    @POST("Friends/UnShareLibrary")
    suspend fun unShareLibraryWithFriend(@Body libraryFriendLink: LibraryFriendLink): Response<Result>







    // ***** Libraries *****
    @GET("Libraries/AdminList")
    suspend fun adminListLibraries(): Response<ResultOf<List<DetailedLibrary>>>

    @GET("Libraries/List")
    suspend fun listLibraries(): Response<ResultOf<List<BasicLibrary>>>





    // ***** Media *****
    @GET("Media/AddToWatchlist/{id}")
    suspend fun addToWatchlist(@Path("id") id: Int): Response<Result>

    @DELETE("Media/DeleteFromWatchlist/{id}")
    suspend fun deleteFromWatchlist(@Path("id") id: Int): Response<Result>

    @GET("Media/GetTitlePermissions/{id}")
    suspend fun getTitlePermissions(@Path("id") id: Int): Response<ResultOf<TitlePermissions>>

    @GET("Media/HomeScreen")
    suspend fun homeScreen(): Response<ResultOf<HomeScreen>>

    @POST("Media/LoadMoreHomeScreenItems")
    suspend fun loadMoreHomeScreenItems(@Body loadMoreHomeScreenListItemsRequest: LoadMoreHomeScreenItemsRequest): Response<ResultOf<List<BasicMedia>>>

    @GET("Media/RequestAccessOverride/{id}")
    suspend fun requestAccessOverride(@Path("id") id: Int): Response<Result>

    @POST("Media/Search")
    suspend fun search(@Body searchRequest: SearchRequest): Response<ResultOf<SearchResults>>

    @POST("Media/SetTitlePermissions")
    suspend fun setTitlePermissions(@Body setTitlePermissionInfo: SetTitlePermission) : Response<Result>

    @POST("Media/UpdatePlaybackProgress")
    suspend fun updatePlaybackProgress(@Body playbackProgress: PlaybackProgress): Response<Result>



    // ***** Movies *****
    @GET("Movies/Details/{id}")
    suspend fun movieDetails(@Path("id") id: Int): Response<ResultOf<DetailedMovie>>






    // ***** Notifications *****
    @DELETE("Notifications/Delete/{id}")
    suspend fun deleteNotification(@Path("id") id: Int): Response<Result>

    @GET("Notifications/List/{start}")
    suspend fun listNotifications(@Path("start") start: Int): Response<ResultOf<List<Notification>>>

    @GET("Notifications/MarkAsRead/{id}")
    suspend fun markNotificationRead(@Path("id") id: Int): Response<Result>




    // ***** Playlists *****
    @POST("Playlists/AddItem")
    suspend fun addItemToPlaylist(@Body addPlaylistItem: AddPlaylistItem): Response<ResultOf<Int>>

    @POST("Playlists/AddSeries")
    suspend fun addSeriesToPlaylist(@Body addSeriesToPlaylistInfo: AddSeriesToPlaylistInfo): Response<Result>

    @POST("Playlists/Create")
    suspend fun createPlaylist(@Body createPlaylist: CreatePlaylist): Response<ResultOf<Int>>

    @DELETE("Playlists/Delete/{id}")
    suspend fun deletePlaylist(@Path("id") id: Int): Response<Result>

    @DELETE("Playlists/DeleteItem/{id}")
    suspend fun deletePlaylistItem(@Path("id") id: Int): Response<Result>

    @GET("Playlists/List")
    suspend fun listPlaylists(): Response<ResultOf<List<BasicPlaylist>>>

    @POST("Playlists/Update")
    suspend fun updatePlaylist(@Body updatesPlaylist: UpdatesPlaylist): Response<Result>

    @GET("Playlists/Details/{id}")
    suspend fun playlistDetails(@Path("id") id: Int): Response<ResultOf<DetailedPlaylist>>

    @POST("Playlists/MoveItemToNewIndex")
    suspend fun movePlaylistItemToNewIndex(@Body movePlaylistItem: MovePlaylistItem): Response<Result>

    @POST("Playlists/SetPlaylistProgress")
    suspend fun setPlaylistProgress(@Body playbackProgress: PlaybackProgress): Response<Result>




    // ***** Profiles *****
    @POST("Profiles/Create")
    suspend fun createProfile(@Body createProfile: CreateProfile): Response<ResultOf<Int>>

    @GET("Profiles/List")
    suspend fun listProfiles(): Response<ResultOf<List<BasicProfile>>>

    @GET("Profiles/Details/{id}")
    suspend fun profileDetails(@Path("id") id: Int): Response<ResultOf<DetailedProfile>>

    @POST("Profiles/Update")
    suspend fun updateProfile(@Body updateProfile: UpdateProfile): Response<Result>

    @Multipart
    @PUT("Profiles/SetProfileAvatarMultipart/{id}")
    suspend fun setProfileAvatar(@Path("id") id: Int, @Part image: MultipartBody.Part): Response<ResultOf<String>>

    @POST("Profiles/LinkToLibrary")
    suspend fun linkProfileLibrary(@Body profileLibraryLink: ProfileLibraryLink): Response<Result>

    @POST("Profiles/UnLinkFromLibrary")
    suspend fun unLinkProfileLibrary(@Body profileLibraryLink: ProfileLibraryLink): Response<Result>

    @DELETE("Profiles/Delete/{id}")
    suspend fun deleteProfile(@Path("id") id: Int): Response<Result>






    // ***** Series *****
    @GET("Series/Details/{id}")
    suspend fun seriesDetails(@Path("id") id: Int): Response<ResultOf<DetailedSeries>>

    @GET("Series/MarkSeriesWatched/{id}")
    suspend fun markSeriesWatched(@Path("id") id: Int): Response<Result>

    @GET("Series/RemoveFromContinueWatching/{id}")
    suspend fun removeFromContinueWatching(@Path("id") id: Int): Response<Result>

    @GET("Series/Subscribe/{id}")
    suspend fun subscribeToSeries(@Path("id") id: Int): Response<Result>

    @DELETE("Series/Unsubscribe/{id}")
    suspend fun unsubscribeFromSeries(@Path("id") id: Int): Response<Result>




    // ***** TMDB *****
    @GET("TMDB/GetMovie/{id}")
    suspend fun getTMDBMovie(@Path("id") id: Int): Response<ResultOf<DetailedTMDB>>

    @GET("TMDB/GetSeries/{id}")
    suspend fun getTMDBSeries(@Path("id") id: Int): Response<ResultOf<DetailedTMDB>>

    @POST("TMDB/RequestTitle")
    suspend fun requestTMDBTitle(@Body titleRequest: TitleRequest): Response<Result>

    @POST("TMDB/CancelTitleRequest")
    suspend fun cancelTMDBTitleRequest(@Body titleRequest: TitleRequest): Response<Result>

    @GET("TMDB/GetPerson/{id}")
    suspend fun getTMDBPerson(@Path("id") id: Int): Response<ResultOf<TMDB_Person>>
}
