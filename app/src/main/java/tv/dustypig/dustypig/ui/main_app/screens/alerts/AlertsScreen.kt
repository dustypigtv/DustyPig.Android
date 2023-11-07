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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.api.models.Notification
import tv.dustypig.dustypig.ui.composables.PreviewBase
import java.util.Date

@Composable
fun AlertsScreen(vm: AlertsViewModel) {
    val uiState by vm.uiState.collectAsState()
    AlertsScreenInternal(uiState = uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertsScreenInternal(uiState: AlertsUIState) {

    val delayTime = 300

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

                    var show by remember { mutableStateOf(true) }

                    val dismissState = rememberDismissState(
                        initialValue = DismissValue.Default,
                        confirmValueChange = {
                            if(it == DismissValue.DismissedToStart) {
                                show = false
                            }
                            it == DismissValue.DismissedToStart
                        },
                        positionalThreshold = { totalDistance -> (totalDistance * 0.5).dp.toPx() }
                    )

                    LaunchedEffect(show) {
                        if(!show) {
                            delay(delayTime.toLong())
                            uiState.onDeleteItem(notification.id)
                        }
                    }

                    AnimatedVisibility(
                        visible = show,
                        exit = shrinkVertically(
                            animationSpec = tween(
                                durationMillis = delayTime,
                            )
                        )
                    ) {
                        SwipeToDismiss(
                            state = dismissState,
                            modifier = Modifier.padding(12.dp),
                            directions = setOf(DismissDirection.EndToStart),
                            background = {
                                val color = when (dismissState.dismissDirection) {
                                    DismissDirection.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                    else -> Color.Transparent
                                }
                                val direction = dismissState.dismissDirection

                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, shape = RoundedCornerShape(4.dp))
                                        .clip(shape = RoundedCornerShape(4.dp)),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (direction == DismissDirection.EndToStart) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = stringResource(R.string.delete),
                                            modifier = Modifier.padding(12.dp, 0.dp)
                                        )
                                    }
                                }
                            },
                            dismissContent = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            uiState.onItemClicked(notification.id)
                                        }
                                        .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp), shape = RoundedCornerShape(4.dp))
                                        .clip(shape = RoundedCornerShape(4.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
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
                                            .size(18.dp)
                                            .padding(4.dp)
                                    ) {
                                        if (!notification.seen) {
                                            Icon(
                                                imageVector = Icons.Filled.Circle,
                                                contentDescription = null,
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }


                }
            }
        }

        if(uiState.busy) {
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
                deepLink = null,
                seen = true,
                timestamp = Date()
            ),
            Notification(
                id = 2,
                profileId = 1,
                title = "Hello",
                message = "Again",
                deepLink = null,
                seen = false,
                timestamp = Date()
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