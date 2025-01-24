package tv.dustypig.dustypig.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.BasicProfile

@Composable
fun Avatar(
    basicProfile: BasicProfile,
    size: Int = 48,
    padding: Int = 0,
    clickable: Boolean = true,
    onClick: () -> Unit = { }
) {
    Avatar(
        imageUrl = basicProfile.avatarUrl!!,
        initials = basicProfile.initials,
        size = size,
        padding = padding,
        clickable = clickable,
        onClick = onClick
    )
}


@Composable
fun Avatar(
    imageUrl: String? = null,
    initials: String? = null,
    size: Int = 48,
    padding: Int = 0,
    clickable: Boolean = true,
    onClick: () -> Unit = { }
) {

    var internalModifier = Modifier
        .size(size.dp)
        .background(color = Color.DarkGray, shape = CircleShape)
        .clip(CircleShape)
        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        .padding(padding.dp)
    if (clickable)
        internalModifier = internalModifier.clickable(onClick = onClick)

    if (imageUrl.isNullOrBlank()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = internalModifier
        ) {
            Text(
                text = initials ?: "",
                color = MaterialTheme.colorScheme.outline,
                fontSize = (size / 2.5).sp
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = internalModifier,
            placeholder = debugPlaceholder(R.drawable.ic_logo_transparent),
            error = painterResource(id = R.drawable.grey_profile),
            contentScale = ContentScale.Crop
        )
    }
}


@Preview
@Composable
private fun AvatarPreview() {
    PreviewBase {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column {
                Avatar(
                    imageUrl = "https://s3.dustypig.tv/user-art-defaults/profile/blue.png",
                    clickable = false
                )
                Spacer(modifier = Modifier.height(10.dp))
                Avatar(
                    initials = "JD",
                    clickable = false
                )
            }
        }
    }
}


@Composable
private fun debugPlaceholder(@DrawableRes debugPreview: Int) =
    if (LocalInspectionMode.current) {
        painterResource(id = debugPreview)
    } else {
        null
    }