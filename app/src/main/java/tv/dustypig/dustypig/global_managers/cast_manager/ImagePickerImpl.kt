package tv.dustypig.dustypig.global_managers.cast_manager

import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.media.ImageHints
import com.google.android.gms.cast.framework.media.ImagePicker
import com.google.android.gms.common.images.WebImage

class ImagePickerImpl : ImagePicker() {
    override fun onPickImage(mediaMetadata: MediaMetadata?, hints: ImageHints): WebImage? {

        if(mediaMetadata == null || !mediaMetadata.hasImages())
            return null

        val images = mediaMetadata.images
        if(images.size == 1)
            return images[0]

        if(hints.type == IMAGE_TYPE_MEDIA_ROUTE_CONTROLLER_DIALOG_BACKGROUND)
            return images[0]

        return images[1]
    }
}