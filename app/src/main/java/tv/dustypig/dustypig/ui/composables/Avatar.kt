package tv.dustypig.dustypig.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicProfile

@Composable
fun Avatar(basicProfile: BasicProfile, onClick: () -> Unit = { }, modifier: Modifier? = Modifier, clickable: Boolean = true) {
   Avatar(imageUrl = basicProfile.avatarUrl!!, onClick = onClick, modifier = modifier, clickable = clickable)
}



@Composable
fun Avatar(imageUrl: String, onClick: () -> Unit = { }, modifier: Modifier? = Modifier, clickable: Boolean = true) {

    var internalModifier = (modifier ?: Modifier)
        .background(color = Color.DarkGray, shape = CircleShape)
        .clip(CircleShape)
        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
    if(clickable)
        internalModifier = internalModifier.clickable(onClick = onClick)


    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = internalModifier,
        placeholder = debugPlaceholder(R.drawable.ic_logo_transparent),
        error = painterResource(id = R.drawable.grey_profile)
    )
}


@Preview
@Composable
private fun AvatarPreview() {
    PreviewBase {
        Avatar(
            imageUrl = "https://s3.dustypig.tv/user-art-defaults/profile/blue.png",
            clickable = false,
            modifier = Modifier.size(48.dp)
        )
    }
}


@Composable
private fun debugPlaceholder(@DrawableRes debugPreview: Int) =
    if (LocalInspectionMode.current) {
        painterResource(id = debugPreview)
    } else {
        null
    }