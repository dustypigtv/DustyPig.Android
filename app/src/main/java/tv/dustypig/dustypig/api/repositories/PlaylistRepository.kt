package tv.dustypig.dustypig.api.repositories

import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.api.models.AddPlaylistItem
import tv.dustypig.dustypig.api.models.CreatePlaylist
import tv.dustypig.dustypig.api.models.MovePlaylistItem
import tv.dustypig.dustypig.api.models.UpdatesPlaylist
import tv.dustypig.dustypig.di.AuthenticatedAPIService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    @AuthenticatedAPIService private val apiService: ApiService,
    authManager: AuthManager
): _RepositoryBase(authManager) {

    suspend fun addItem(addPlaylistItem: AddPlaylistItem) = wrapAPICallWithReturnSimpleValue { apiService.addItemToPlaylist(addPlaylistItem) }

    suspend fun addSeries(addPlaylistItem: AddPlaylistItem) = wrapAPICall { apiService.addSeriesToPlaylist(addPlaylistItem) }

    suspend fun create(createPlaylist: CreatePlaylist) = wrapAPICallWithReturnSimpleValue { apiService.createPlaylist(createPlaylist) }

    suspend fun deletePlaylist(id: Int) = wrapAPICall { apiService.deletePlaylist(id) }

    suspend fun deleteItem(id: Int) = wrapAPICall { apiService.deletePlaylistItem(id) }

    suspend fun list() = wrapAPICallWithReturnData { apiService.listPlaylists() }

    suspend fun moveItem(id: Int, newIndex: Int) = wrapAPICall { apiService.movePlaylistItemToNewIndex(MovePlaylistItem(id, newIndex)) }

    suspend fun rename(updatesPlaylist: UpdatesPlaylist) = wrapAPICall { apiService.updatePlaylist(updatesPlaylist) }

    suspend fun details(id: Int) = wrapAPICallWithReturnData { apiService.playlistDetails(id) }

}