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
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.UserLock
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.OverrideRequestStatus
import tv.dustypig.dustypig.global_managers.download_manager.DownloadManager
import tv.dustypig.dustypig.global_managers.download_manager.DownloadStatus
import tv.dustypig.dustypig.ui.isTablet




data class TitleInfoData(
    val mediaId: Int = 0,
    val playClick: () -> Unit = { },
    val toggleWatchList: () -> Unit = { },
    val download: () -> Unit = { },
    val addToPlaylist: () -> Unit = { },
    val markWatched: () -> Unit = { },
    val requestAccess:() -> Unit = { },
    val manageClick: () -> Unit = { },
    val title: String = "",
    val year: String = "",
    val rated: String = "",
    val length: String = "",
    val overview: String = "",
    val canManage: Boolean = false,
    val canPlay: Boolean = false,
    val partiallyPlayed: Boolean = false,
    val markWatchedBusy: Boolean = false,
    val inWatchList: Boolean = false,
    val watchListBusy: Boolean = false,
    val seasonEpisode: String = "",
    val episodeTitle: String = "",
    val accessRequestStatus: OverrideRequestStatus = OverrideRequestStatus.NotRequested,
    val accessRequestBusy: Boolean = false,
    val downloadManager: DownloadManager
)


@Composable
fun ActionButton(onClick: () -> Unit, caption: String, icon: ImageVector) {
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
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TitleInfoLayout(info: TitleInfoData) {

    //Align buttons to center for phone, left for tablet
    val configuration = LocalConfiguration.current
    val alignment = if(configuration.isTablet()) Alignment.Start else Alignment.CenterHorizontally
    val modifier = if(configuration.isTablet()) Modifier.width(320.dp) else Modifier.fillMaxWidth()
    val buttonPadding = if(configuration.isTablet()) PaddingValues(0.dp, 0.dp  ) else PaddingValues(16.dp, 0.dp)

    val playButtonText =
        if (info.partiallyPlayed)
            stringResource(R.string.resume_season_episode, info.seasonEpisode).trim()
        else
            stringResource(R.string.play_season_episode, info.seasonEpisode).trim()


    //This will leave at minimum just the colon, so check if
    //length > 1 before displaying
    val epHeader = "${info.seasonEpisode}: ${info.episodeTitle}".trim()



    val status = info.downloadManager
        .downloads
        .collectAsStateWithLifecycle(initialValue = listOf())
        .value
        .firstOrNull{ it.mediaId == info.mediaId }
        ?.status
    val downloadIcon = when(status) {
        DownloadStatus.Finished -> Icons.Filled.DownloadDone
        null -> Icons.Filled.Download
        else -> Icons.Filled.Downloading
    }
    val downloadText = when(status) {
        DownloadStatus.Finished -> stringResource(R.string.downloaded)
        null -> stringResource(R.string.download)
        else -> stringResource(R.string.downloading)
    }


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
                text = info.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(info.year.isNotBlank()) {
                    Text(
                        text = info.year,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                if (info.rated.isNotBlank()) {
                    Text(
                        text = info.rated,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .border(width = 1.dp, color = Color.White, shape = RectangleShape)
                            .padding(8.dp, 4.dp)
                    )
                }

                if (info.length.isNotBlank()) {
                    Text(
                        text = info.length,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        if (info.canManage) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(48.dp)
                    .clickable { }
            ) {
                IconButton(onClick = info.manageClick) {
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

    if (info.canPlay) {
        Column(
            horizontalAlignment = alignment,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = info.playClick,
                modifier = modifier.padding(buttonPadding)
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Play,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = playButtonText)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top,
                modifier = modifier.padding(0.dp, 12.dp)
            ) {


                if(info.watchListBusy) {

                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(12.dp)
                    )

                } else {
                    ActionButton(
                        onClick = info.toggleWatchList,
                        caption = stringResource(R.string.watchlist),
                        icon = if (info.inWatchList) Icons.Filled.Check else Icons.Filled.Add
                    )
                }

                ActionButton(
                    onClick = info.download,
                    caption = downloadText,
                    icon = downloadIcon
                )

                ActionButton(
                    onClick = info.addToPlaylist,
                    caption = stringResource(R.string.add_to_playlist),
                    icon = Icons.Filled.PlaylistAdd
                )

                if(info.partiallyPlayed) {
                    if(info.markWatchedBusy) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(12.dp)
                            )
                            Text(
                                text = stringResource(R.string.mark_watched),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(58.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        ActionButton(
                            onClick = info.markWatched,
                            caption = stringResource(R.string.mark_watched),
                            icon = Icons.Filled.RemoveRedEye
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    } else {

        val btnTxt = when(info.accessRequestStatus) {
            OverrideRequestStatus.NotRequested -> stringResource(R.string.request_access)
            OverrideRequestStatus.Requested -> stringResource(R.string.access_already_requested)
            OverrideRequestStatus.Denied -> stringResource(R.string.access_denied)
            OverrideRequestStatus.Granted -> stringResource(R.string.if_you_see_this_it_s_a_bug)
        }

        Column(
            horizontalAlignment = alignment,
            modifier = Modifier.fillMaxWidth()
        ) {
            if(info.accessRequestBusy) {
             CircularProgressIndicator()
            } else {
                Button(
                    onClick = info.requestAccess,
                    modifier = Modifier.padding(buttonPadding),
                    enabled = info.accessRequestStatus == OverrideRequestStatus.NotRequested
                ) {
                    Text(text = btnTxt)
                }
            }
        }
    }


    if(epHeader.length > 1) {
        Text(text = epHeader, textDecoration = TextDecoration.Underline)
        Spacer(modifier = Modifier.height(8.dp))
    }

    Text(text = info.overview)
}

