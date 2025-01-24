package tv.dustypig.dustypig.ui.main_app.screens.alerts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.Notification
import tv.dustypig.dustypig.api.models.NotificationTypes
import tv.dustypig.dustypig.ui.composables.PreviewBase
import tv.dustypig.dustypig.ui.theme.DarkRed
import java.util.Date


private val dismissPadding = 12.dp

@Composable
fun AlertsScreen(vm: AlertsViewModel) {
    val uiState by vm.uiState.collectAsState()
    AlertsScreenInternal(uiState = uiState)
}


@Composable
private fun AlertCard(
    uiState: AlertsUIState,
    notification: Notification
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    uiState.onItemClicked(notification.id)
                }
                .background(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                .clip(shape = RoundedCornerShape(4.dp))
        ) {

            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp, 4.dp)
            )

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp, 4.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clickable {
                    uiState.onItemClicked(notification.id)
                }

        ) {
            if (!notification.seen) {
                Icon(
                    imageVector = Icons.Filled.Circle,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }

    }
}


@Composable
private fun DismissBackground(dismissState: SwipeToDismissBoxState) {

    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> DarkRed
        else -> Color.Transparent
    }

    val configuration = LocalConfiguration.current

    val boxWidth = configuration.screenWidthDp.dp - dismissPadding * 2
    val slide = boxWidth * dismissState.progress
    val xOffset = if (slide > 60.dp)
        min(dismissPadding * 4, (slide - dismissPadding * 3) / 2)
    else
        dismissPadding

    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(4.dp))
            .fillMaxSize()
            .background(color),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        if (dismissState.progress < 0.99f) {
            Icon(
                Icons.Outlined.Delete,
                tint = Color.White,
                contentDescription = "delete",
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = -xOffset)
            )
        }
    }
}


@Composable
private fun AlertItem(
    notification: Notification,
    uiState: AlertsUIState
) {
    val delayTime = 300

    var show by remember { mutableStateOf(true) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    show = false
                    uiState.onDeleteItem(notification.id)
                    true
                }

                else -> false
            }
        },
        positionalThreshold = { it * 0.5f }
    )

    AnimatedVisibility(
        visible = show,
        exit = shrinkVertically(
            animationSpec = tween(
                durationMillis = delayTime,
            )
        )
    ) {
        SwipeToDismissBox(
            modifier = Modifier
                .padding(dismissPadding),
            state = dismissState,
            backgroundContent = { DismissBackground(dismissState) },
            enableDismissFromStartToEnd = false,
            content = { AlertCard(uiState, notification) }
        )
    }
}


@Composable
private fun AlertsScreenInternal(uiState: AlertsUIState) {


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        if (uiState.notifications.isEmpty()) {
            Text(
                text = stringResource(R.string.no_alerts),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val lazyColumnState = rememberLazyListState()

            LazyColumn(
                state = lazyColumnState,
            ) {
                items(uiState.notifications, key = { it.id }) { notification ->

//                    var show by remember { mutableStateOf(true) }

//                    val dismissState = rememberDismissState(
//                        initialValue = DismissValue.Default,
//                        confirmValueChange = {
//                            if(it == DismissValue.DismissedToStart) {
//                                show = false
//                            }
//                            it == DismissValue.DismissedToStart
//                        },
//                        positionalThreshold = { totalDistance -> (totalDistance * 0.5).dp.toPx() }
//                    )

//                    LaunchedEffect(show) {
//                        if(!show) {
//                            delay(delayTime.toLong())
//                            uiState.onDeleteItem(notification.id)
//                        }
//                    }

//                    AnimatedVisibility(
//                        visible = show,
//                        exit = shrinkVertically(
//                            animationSpec = tween(
//                                durationMillis = delayTime,
//                            )
//                        )
//                    ) {


                    AlertItem(notification = notification, uiState = uiState)


                }

            }
        }

        if (uiState.busy) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview
@Composable
private fun AlertsViewModelPreview() {
    val uiState = AlertsUIState(
        notifications = listOf(
            Notification(
                id = 1,
                profileId = 1,
                title = "Hello",
                message = "World",
                seen = true,
                timestamp = Date(),
                notificationType = NotificationTypes.NewMediaPending,
                mediaId = null,
                mediaType = null,
                friendshipId = null
            ),
            Notification(
                id = 2,
                profileId = 1,
                title = "Hello",
                message = "Again",
                seen = false,
                timestamp = Date(),
                notificationType = NotificationTypes.FriendshipInvited,
                mediaId = null,
                mediaType = null,
                friendshipId = null
            )
        )
    )

    PreviewBase {
        Scaffold {
            Box(modifier = Modifier.padding(it)) {
                AlertsScreenInternal(uiState = uiState)
            }
        }
    }
}