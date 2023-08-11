package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicMedia


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BasicMediaView(
    basicMedia: BasicMedia,
    onClicked: () -> Unit = { }
) {
    GlideImage(
        model = basicMedia.artwork_url,
        contentDescription = null,
        modifier = Modifier
            .size(100.dp, 150.dp)
            .clickable { onClicked.invoke() }
    ){
        it
            .placeholder(R.drawable.placeholder_tall)
            .error(R.drawable.error_tall)
            .override(100, 150)
    }
}