package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import tv.dustypig.dustypig.api.models.BasicProfile

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Avatar(basicProfile: BasicProfile, onClick: () -> Unit = { }, modifier: Modifier? = Modifier) {

    val internalModifier = (modifier ?: Modifier)
        .clip(CircleShape)
        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
        .clickable(onClick = onClick)

    GlideImage(model = basicProfile.avatarUrl,
        contentDescription = basicProfile.name,
        modifier = internalModifier){
        it.diskCacheStrategy(DiskCacheStrategy.NONE)
    }
}