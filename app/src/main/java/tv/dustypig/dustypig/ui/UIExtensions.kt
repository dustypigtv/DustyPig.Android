package tv.dustypig.dustypig.ui

import android.content.Context
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import androidx.window.layout.WindowMetricsCalculator
import kotlin.math.min


fun Context.isTablet(): Boolean {
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    val width = metrics.bounds.width().toFloat()
    val height = metrics.bounds.height().toFloat()
    val density = resources.displayMetrics.density
    return (min(width, height) / density) >= 600
}


//fun Context.findActivity(): ComponentActivity {
//    var context = this
//    while (context is ContextWrapper) {
//        if (context is ComponentActivity) return context
//        context = context.baseContext
//    }
//    throw IllegalStateException("Picture in picture should be called in the context of an Activity")
//}


inline fun <T : Any> LazyGridScope.itemsExt(
    items: LazyPagingItems<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline span: (LazyGridItemSpanScope.(item: T?) -> GridItemSpan)? = null,
    noinline contentType: (item: T?) -> Any? = { null },
    crossinline itemContent: @Composable LazyGridItemScope.(item: T?) -> Unit
) = items(
    count = items.itemCount,
    key = if (key != null) { index: Int -> items[index]?.let(key) ?: index } else null,
    span = if (span != null) {
        { span(items[it]) }
    } else null,
    contentType = { index: Int -> contentType(items[index]) }
) {
    itemContent(items[it])
}