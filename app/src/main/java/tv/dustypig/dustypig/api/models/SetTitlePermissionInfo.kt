package tv.dustypig.dustypig.api.models

import com.google.gson.annotations.SerializedName

data class SetTitlePermissionInfo(
    @SerializedName("media_id") val mediaId: Int,
    @SerializedName("profile_id") val profileId: Int,
    @SerializedName("override_state") val overrideState: OverrideState
)
