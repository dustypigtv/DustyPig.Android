package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tv.dustypig.dustypig.ui.theme.DimOverlay

@Composable
private fun CountRow(count: Int, newDownloadCount: MutableIntState) {

    val text = if(count == 0) "0 (Remove Downloads)" else count.toString()
    val backgroundColor = if(newDownloadCount.intValue == count) DimOverlay else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { newDownloadCount.intValue = count },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(0.dp, 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MultiDownloadDialog(onSave: (Int) -> Unit, title:String, itemName: String, currentDownloadCount: Int) {

    val newDownloadCount = remember {
        mutableIntStateOf(currentDownloadCount)
    }

    val counts = sequence {
        yieldAll(listOf(0, 1, 3))
        yieldAll(5..100 step 5)
    }

    val listState = rememberLazyListState()
    if(currentDownloadCount > 5) {
        LaunchedEffect(key1 = false) {
            try {
                listState.scrollToItem(index = (counts.indexOf(currentDownloadCount) - 1).coerceAtLeast(0))
            } catch (_: Exception) {
            }
        }
    }

    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        onDismissRequest = { onSave(currentDownloadCount) },
        title = { Text(title) },
        text = {
            Column {
                Text("How many unwatched ${itemName}s do you want to keep downloaded?")
                Spacer(modifier = Modifier.height(24.dp))
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    for(count in counts) {
                        item {
                            CountRow(count = count, newDownloadCount = newDownloadCount)
                        }
                    }

                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(newDownloadCount.intValue) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onSave(currentDownloadCount) }) {
                Text("Cancel")
            }
        }
    )

}
