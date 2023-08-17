package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.UserLock
import tv.dustypig.dustypig.ui.isTablet


@Composable
private fun ActionButton(onClick: () -> Unit, caption: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(58.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TitleInfoLayout(
    playClick: () -> Unit,
    toggleWatchList: () -> Unit,
    download: () -> Unit,
    addToPlaylist: () -> Unit,
    markWatched: () -> Unit,
    requestAccess:() -> Unit,
    manageClick: () -> Unit,
    title: String,
    year: String,
    rated: String,
    length: String,
    description: String,
    canManage: Boolean,
    canPlay: Boolean,
    partiallyPlayed: Boolean,
    inWatchList: Boolean
) {

    //Align buttons to center for phone, left for tablet
    val configuration = LocalConfiguration.current
    val alignment = if(configuration.isTablet()) Alignment.Start else Alignment.CenterHorizontally
    val modifier = if(configuration.isTablet()) Modifier.width(320.dp) else Modifier.fillMaxWidth()
    val buttonPadding = if(configuration.isTablet()) PaddingValues(0.dp, 0.dp  ) else PaddingValues(16.dp, 0.dp)

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(8.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1F)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(year.isNotBlank()) {
                    Text(
                        text = year,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                if (rated.isNotBlank()) {
                    Text(
                        text = rated,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .border(width = 1.dp, color = Color.White, shape = RectangleShape)
                            .padding(8.dp, 4.dp)
                    )
                }

                if (length.isNotBlank()) {
                    Text(
                        text = length,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        if (canManage) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(48.dp)
                    .clickable { }
            ) {
                IconButton(onClick = manageClick) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.UserLock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }


    }

    Spacer(modifier = Modifier.height(12.dp))

    if (canPlay) {
        Column(
            horizontalAlignment = alignment,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = playClick,
                modifier = modifier.padding(buttonPadding)
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Play,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = if (partiallyPlayed) "Resume" else "Play")
            }

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top,
                modifier = modifier.padding(0.dp, 12.dp)
            ) {


                ActionButton(
                    onClick = toggleWatchList,
                    caption = "Watchlist",
                    icon = if (inWatchList) Icons.Filled.Check else Icons.Filled.Add)

                ActionButton(
                    onClick = download,
                    caption = "Download",
                    icon = Icons.Filled.Download
                )

                ActionButton(
                    onClick = addToPlaylist,
                    caption = "Add to Playlist",
                    icon = Icons.Filled.PlaylistAdd
                )

                if(partiallyPlayed) {
                    ActionButton(
                        onClick = markWatched,
                        caption = "Mark Watched",
                        icon = Icons.Filled.RemoveRedEye
                    )
                }
            }
        }
    } else {
        Button(
            onClick = requestAccess,
            modifier = Modifier.padding(buttonPadding)
        ) {
            Text(text = "Request Access")
        }
    }

    Text(text = description)
}

