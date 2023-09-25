package tv.dustypig.dustypig.ui.main_app.screens.home.show_more

import androidx.paging.PagingSource
import androidx.paging.PagingState
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.HomeScreenList
import tv.dustypig.dustypig.api.models.LoadMoreHomeScreenItemsRequest
import tv.dustypig.dustypig.api.repositories.MediaRepository
import tv.dustypig.dustypig.logToCrashlytics

class ShowMorePagingSource (
    private val listId: Long,
    private val mediaRepository: MediaRepository
): PagingSource<Int, BasicMedia>() {

    companion object {
        var showMoreData: HomeScreenList = HomeScreenList(0, "", listOf())
    }


    override fun getRefreshKey(state: PagingState<Int, BasicMedia>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BasicMedia> {

        try {
            val key = params.key ?: 0
            val start = key * 25

            val data = if(key == 0)
                showMoreData.items
            else
                mediaRepository.loadMoreHomeScreenItems(LoadMoreHomeScreenItemsRequest(listId, start))

            val nextKey = when {
                data.size < 25 -> null
                else -> key + 1
            }

            return LoadResult.Page(
                prevKey = null,
                nextKey = nextKey,
                data = data
            )
        } catch (ex: Exception) {

            ex.logToCrashlytics()

            return LoadResult.Page(
                prevKey = null,
                nextKey = null,
                data = listOf()
            )
        }
    }
}