package tv.dustypig.dustypig.ui.composables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlin.math.max
import kotlin.math.sqrt

@Composable
fun AvatarEditor(
    enabled: Boolean = true,
    size: Double = 164.0,
    currentAvatar: String = "",
    onChanged: (String) -> Unit,
    onException: (Exception?) -> Unit
) {
    val context = LocalContext.current

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            onChanged(result.getUriFilePath(context) ?: "")
        } else {
            onException(result.error)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        val cropOptions = CropImageContractOptions(
            uri = uri,
            cropImageOptions = CropImageOptions(
                outputCompressQuality = 100,
                cropShape = CropImageView.CropShape.OVAL,
                aspectRatioX = 1,
                aspectRatioY = 1,
                fixAspectRatio = true
            )
        )
        imageCropLauncher.launch(cropOptions)
    }



    //IconButtonTokens.StateLayerSize = 40
    //I don't see it in the source, but there is an extra dp somewhere, so use 41
    val iconSize = 41.0

    val circleSize = max(iconSize * 4, size)

    var circlePadding = 0.0
    val fact = (sqrt(2.0) - 1) / 2
    val distance = circleSize * fact
    val iconDiagonal = iconSize * sqrt(2.0)
    if(distance < iconDiagonal) {
        circlePadding = max(0.0, iconDiagonal - distance)
    }


    Box(
        modifier = Modifier.size(circleSize.dp),
        contentAlignment = Alignment.Center
    ) {

        Avatar(
            imageUrl = currentAvatar,
            size = circleSize.toInt(),
            padding = circlePadding.toInt()
        )
        IconButton(
            onClick = {
                imagePickerLauncher.launch("image/*")
            },
            modifier = Modifier.align(Alignment.TopEnd),
            enabled = enabled
        ) {
            TintedIcon(imageVector = Icons.Filled.Edit)
        }
    }
}

@Preview
@Composable
private fun AvatarEditorPreview() {

    PreviewBase {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarEditor(
                onChanged = { },
                onException = { }
            )
        }
    }
}