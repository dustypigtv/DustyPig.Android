package tv.dustypig.dustypig.ui.composables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import tv.dustypig.dustypig.R
import tv.dustypig.dustypig.ui.main_app.screens.settings.profiles_settings.edit_profile.EditProfileViewModel

@Composable
fun AvatarEditor(
    enabled: Boolean = true,
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

    val imagePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            val cropOptions = CropImageContractOptions(
                uri = uri,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeCamera = false,
                    imageSourceIncludeGallery = true,
                    outputCompressQuality = 100,
                    cropShape = CropImageView.CropShape.OVAL,
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    fixAspectRatio = true
                )
            )
            imageCropLauncher.launch(cropOptions)
        }




    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {

        AsyncImage(
            //model = currentAvatar,
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentAvatar)
                .crossfade(true)
                .build(),
            contentDescription = null,
            error = painterResource(id = R.drawable.grey_profile),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.DarkGray, shape = CircleShape)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
    }

    Row (
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = {
                imagePickerLauncher.launch("image/*")
            },
            enabled = enabled
        ) {
            TintedIcon(imageVector = Icons.Default.ImageSearch)
        }

        IconButton(
            onClick = {
                onChanged(EditProfileViewModel.getRandomAvatar(currentAvatar))
            },
            enabled = enabled
        ) {
            TintedIcon(imageVector = Icons.Default.Delete)
        }

        Spacer(modifier = Modifier.weight(1f))

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