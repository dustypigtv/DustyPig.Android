package tv.dustypig.dustypig.ui.main_app.screens.show_more

import androidx.paging.PagingSource
import androidx.paging.PagingState
import tv.dustypig.dustypig.api.ThePig
import tv.dustypig.dustypig.api.models.BasicMedia
import tv.dustypig.dustypig.api.models.LoadMoreHomeScreenItemsRequest
import tv.dustypig.dustypig.api.throwIfError

class ShowMorePagingSource (
    private val listId: Long
): PagingSource<Int, BasicMedia>() {

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

            val response = ThePig.api.loadMoreHomeScreenItems(LoadMoreHomeScreenItemsRequest(listId, start))
            response.throwIfError()
            val data = response.body()!!.data

            val nextKey = when {
                data.size < 25 -> null
                else -> key + 1
            }

            return LoadResult.Page(
                prevKey = null,
                nextKey = nextKey,
                data = data
            )
        } catch (_: Exception) {

            return LoadResult.Page(
                prevKey = null,
                nextKey = null,
                data = listOf()
            )
        }
    }
}