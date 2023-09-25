package tv.dustypig.dustypig.ui.composables

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun TintedIcon (imageVector: ImageVector, modifier:Modifier = Modifier) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = modifier,
        tint = MaterialTheme.colorScheme.surfaceTint
    )
}